package com.company;

import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class Main {
    private static int tcpPort = 10000;

    public static void main(String[] args) {
        TcpServer server = new TcpServer(9999);
        Queue<ThreadStreamerPair> threadQueue = new LinkedList<>();

        while (true) {
            Utils.log("Main: " + "Waiting for HTTP server . . .");
            TcpClient httpServer = new TcpClient(server.accept());
            Utils.log("Main: " + "HTTP server TCP connected");

            while (true) {
                Utils.log("Main: " + "Waiting for RTP client information . . .");
                final int addrLength;

                try {
                    addrLength = httpServer.receiveInt();
                    final String clientJsonStr = httpServer.receiveString(addrLength);

                    Utils.log("Main: " + "Received JSON: " + clientJsonStr);

                    final JSONObject clientJson = new JSONObject(clientJsonStr);
                    final String address = clientJson.getString("address");

                    if (!clientJson.has("rtp_port")) {
                        Utils.log("Main: " + "Client remove start");
                        for (ThreadStreamerPair pair : threadQueue) {
                            if (pair.getStreamer().getAddress().equals(address)) {
                                Utils.log("Main: " + "Client thread found");
                                pair.getStreamer().exit();
                                try {
                                    pair.getThread().join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Utils.log("Main: " + "Client removed");
                                break;
                            }
                        }
                        Utils.log("Main: " + "Client remove finish");
                        continue;
                    }

                    final int rtpPort = clientJson.getInt("rtp_port");

                    Streamer streamer = new Streamer(address, tcpPort, rtpPort);
                    Thread t = new Thread(streamer);
                    threadQueue.add(new ThreadStreamerPair(t, streamer));
                    t.start();
                    httpServer.sendInt(tcpPort);

                    tcpPort++;
                } catch (IOException e) {
                    Utils.log("Main: " + "Lost connection with server, cleaning up . . .");
                    for (ThreadStreamerPair pair : threadQueue) {
                        pair.getStreamer().exit();
                    }
                    for (ThreadStreamerPair pair : threadQueue) {
                        try {
                            pair.getThread().join();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    Utils.log("Main: " + "Cleaning finished");
                    break;
                }
            }
        }
    }
}
