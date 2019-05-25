/* This file is based on
 * http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
 * Please see the site for license information.
 */
package jlibrtpDemos;

import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import jlibrtp.*;

/**
 * @author Arne Kepp
 */
public class SoundReceiverDemo implements RTPAppIntf {
    //test
    RTPSession rtpSession = null;
    private Position curPosition;
    byte[] abData = null;
    int nBytesRead = 0;
    int pktCount = 0;
    int dataCount = 0;
    int offsetCount = 0;
    SourceDataLine auline;

    enum Position {
        LEFT, RIGHT, NORMAL
    }


    public void receiveData(DataFrame frame, Participant p) {
        if (auline != null) {
            byte[] data = frame.getConcatenatedData();
            auline.write(data, 0, data.length);
        }
        pktCount++;
    }

    public void userEvent(int type, Participant[] participant) {
        //Do nothing
    }

    public int frameSize(int payloadType) {
        return 1;
    }

    public SoundReceiverDemo(int rtpPort, int rtcpPort) {
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;

        try {
            rtpSocket = new DatagramSocket(rtpPort);
            rtcpSocket = new DatagramSocket(rtcpPort);
        } catch (Exception e) {
            System.out.println("RTPSession failed to obtain port");
        }

        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession.naivePktReception(true);
        rtpSession.RTPSessionRegister(this, null, null);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Setup");

        int rtpPort = 16384;

        SoundReceiverDemo aDemo = new SoundReceiverDemo(rtpPort, rtpPort + 1);

        aDemo.doStuff();
        System.out.println("Done");
    }

    public void doStuff() {
        System.out.println("-> ReceiverDemo.doStuff()");
        AudioFormat.Encoding encoding = new AudioFormat.Encoding("PCM_SIGNED");
        AudioFormat format = new AudioFormat(encoding, 44100.0f, 16, 2, 4, 44100.0f, false);
        System.out.println(format.toString());
        auline = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (auline.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl pan = (FloatControl) auline
                    .getControl(FloatControl.Type.PAN);
            if (this.curPosition == Position.RIGHT)
                pan.setValue(1.0f);
            else if (this.curPosition == Position.LEFT)
                pan.setValue(-1.0f);
        }

        auline.start();
        try {
            while (nBytesRead != -1) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        } finally {
            auline.drain();
            auline.close();
        }
    }
}
