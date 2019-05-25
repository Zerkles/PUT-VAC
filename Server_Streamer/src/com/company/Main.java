package com.company;


import org.json.JSONObject;

import java.io.IOException;

public class Main {
    static Thread t;

    public static void main(String[] args) {
        TcpServer server = new TcpServer(49152);
        RtpServer streamer = new RtpServer(49152);

        while(true) {
            Utils.log("");
            Utils.log("Waiting for TCP client address . . .");
            TcpClient client = new TcpClient(server.accept());
            Utils.log("TCP client connected");

            Utils.log("Waiting for RTP client information . . .");
            final int addrLength;

            try {
                addrLength = client.receiveInt();
                final String clientJsonStr = client.receiveString(addrLength);
                Utils.log("Received JSON: " + clientJsonStr);
                final JSONObject clientJson = new JSONObject(clientJsonStr);
                final String address = clientJson.getString("address");
                final int rtp_port = clientJson.getInt("rtp_port");

                Utils.log("RTP client address: " + address + ":" + rtp_port);
                streamer.addParticipant(address, rtp_port);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            Utils.log("Got RTP client information");

            new Thread(streamer);
            while (true) {
                try {
                    // Utils.log("Waiting for data . . .");
                    final int length = client.receiveInt();
                    if (length < 0 || length > 2 * 96000 || length % 2 != 0) {
                        continue;
                    }
                    byte[] data = client.receiveBytes(length);
                    // Utils.log("Received sound of length: " + length);

                    if (t != null) {
                        streamer.stop();
                        t.join();
                    }
                    streamer.feed(data);
                    t = new Thread(streamer);
                    t.start();
                } catch (InterruptedException | IOException e) {
                    Utils.log("TCP client disconnected");
                    break;
                }
            }
        }

    }
}
