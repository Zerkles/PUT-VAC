package com.company;

import org.json.JSONObject;

import java.io.IOException;

public class Streamer implements Runnable {
    private String address;
    private int tcpPort;
    private int rtpPort;
    private Thread t;

    Streamer(String address, int tcpPort, int rtpPort) {
        this.address = address;
        this.tcpPort = tcpPort;
        this.rtpPort = rtpPort;
    }

    public void run() {
        Utils.log("Streamer: " + "RTP streamer setup . . .");
        Utils.log("Streamer: " + "Server connection address: 127.0.0.1:" + tcpPort);
        Utils.log("Streamer: " + "RTP client address: " + address + ":" + rtpPort);
        Utils.log("Streamer: " + "RTP server address: 0.0.0.0:" + (rtpPort + 2));
        TcpServer server = new TcpServer(tcpPort);
        RtpServer streamer = new RtpServer(rtpPort + 2);
        streamer.addParticipant(address, rtpPort);
        TcpClient client = new TcpClient(server.accept());
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
                streamer.stop();
                break;
            }
        }
        Utils.log("Streamer: " + "Thread finished");
    }
}
