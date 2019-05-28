package com.company;

import org.json.JSONObject;

import java.io.IOException;

public class Streamer implements Runnable {
    private String address;
    private int tcpPort;
    private int rtpPort;
    private Thread t;

    private TcpServer server;
    private TcpClient client;
    private RtpServer streamer;

    Streamer(String address, int tcpPort, int rtpPort) {
        this.address = address;
        this.tcpPort = tcpPort;
        this.rtpPort = rtpPort;
    }

    void exit() {
        if (client != null) {
            client.close();
            Utils.log("Streamer: " + "Client socket closed");
        }
        if (server != null) {
            server.close();
            Utils.log("Streamer: " + "Server socket closed");
        }
        if (streamer != null){
            streamer.stop();
            streamer.shutdown();
            Utils.log("Streamer: " + "Streamer stopped");
        }
    }

    public void run() {
        Utils.log("Streamer: " + "RTP streamer setup . . .");
        Utils.log("Streamer: " + "Server connection address: 127.0.0.1:" + tcpPort);
        Utils.log("Streamer: " + "RTP client address: " + address + ":" + rtpPort);
        Utils.log("Streamer: " + "RTP server address: 0.0.0.0:" + (rtpPort + 2));
        server = new TcpServer(tcpPort);
        streamer = new RtpServer(rtpPort + 2);
        streamer.addParticipant(address, rtpPort);
        client = new TcpClient(server.accept());
        Utils.log("Streamer: " + "RTP streamer setup finish");

        while (true) {
            try {
                final int length = client.receiveInt();
                if (length <= 0 || length > 2 * 96000 || length % 2 != 0) {
                    continue;
                }
                byte[] data = client.receiveBytes(length);
                streamer.feed(data);

                if (t == null) {
                    t = new Thread(streamer);
                    t.start();
                }
            } catch (IOException e) {
                Utils.log("Streamer: " + "TCP client disconnected");
                this.exit();
                break;
            }
        }
        Utils.log("Streamer: " + "Thread finished");
    }

    public String getAddress() {
        return address;
    }
}
