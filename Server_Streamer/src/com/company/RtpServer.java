package com.company;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.net.DatagramSocket;

public class RtpServer implements RTPAppIntf, Runnable {
    private RTPSession rtpSession;
    private static final int EXTERNAL_BUFFER_SIZE = 1024;
    private ByteArrayInputStream dataIn;

    // Audio configuration
    private AudioFormat.Encoding encoding = new AudioFormat.Encoding("PCM_SIGNED");
    private boolean stop = false;
    private byte[] data;


    RtpServer(int rtpPort) {
        DatagramSocket rtpSocket;
        DatagramSocket rtcpSocket;

        try {
            Utils.log(rtpPort);
            rtpSocket = new DatagramSocket(rtpPort);
            rtcpSocket = new DatagramSocket(rtpPort + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession.RTPSessionRegister(this, null, null);
        Utils.log("CNAME: " + rtpSession.CNAME());
    }

    public void addParticipant(String client, int rtpPort) {
        Participant p = new Participant(client, rtpPort, rtpPort + 1);
        rtpSession.addParticipant(p);
    }

    @Override
    public void receiveData(DataFrame frame, Participant participant) {
        // Unused
    }

    @Override
    public void userEvent(int type, Participant[] participant) {
        // Unused
    }

    @Override
    public int frameSize(int payloadType) {
        return 1;
    }

    void feed(byte[] data_) {
        this.data = data_;
        dataIn = new ByteArrayInputStream(data_);
    }

    void stop() {
        stop = true;
    }

    public void run() {
        int nBytesRead;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        while (!stop) {
            nBytesRead = dataIn.read(abData, 0, abData.length);
            if (nBytesRead == -1) {
                dataIn = new ByteArrayInputStream(data);
                continue;
            }

            if (nBytesRead >= 0) {
                rtpSession.sendData(abData);
            }
        }

        stop = false;
    }
}
