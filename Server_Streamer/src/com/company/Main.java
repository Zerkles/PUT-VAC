package com.company;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    private static int tcpPort = 10000;

    public static void main(String[] args) {
        TcpServer server = new TcpServer(49152);

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
                    final int rtpPort = clientJson.getInt("rtp_port");

                    Thread t = new Thread(new Streamer(address, tcpPort, rtpPort));
                    t.start();
                    httpServer.sendInt(tcpPort);

                    tcpPort++;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
