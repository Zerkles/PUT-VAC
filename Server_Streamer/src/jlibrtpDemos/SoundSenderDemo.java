/* This file is based on
 * http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
 * Please see the site for license information.
 */

package jlibrtpDemos;


import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.lang.String;
import java.net.DatagramSocket;
import java.util.Enumeration;

import jlibrtp.*;

/**
 * @author Arne Kepp
 */
public class SoundSenderDemo implements RTPAppIntf {
    public RTPSession rtpSession = null;
    static int pktCount = 0;
    static int dataCount = 0;
    private String filename;
    private final int EXTERNAL_BUFFER_SIZE = 1024;
    //SourceDataLine auline;
    private Position curPosition;
    boolean local;

    enum Position {
        LEFT, RIGHT, NORMAL
    }

    ;

    public SoundSenderDemo(boolean isLocal) {
        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;

        try {
            rtpSocket = new DatagramSocket(49152);
            rtcpSocket = new DatagramSocket(49153);
        } catch (Exception e) {
            System.out.println("RTPSession failed to obtain port");
        }


        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession.RTPSessionRegister(this, null, null);
        System.out.println("CNAME: " + rtpSession.CNAME());
        this.local = isLocal;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "]" + args[i]);
        }

        if (args.length == 0) {
            args = new String[4];
            args[1] = "192.168.43.118";
            args[0] = "G:\\OneDrive - student.put.poznan.pl\\Prace studia\\Projekty\\PUT-VAC_Local\\VAC_Server_Streamer\\wilhelm.wav";
            args[2] = "49154";
            args[3] = "49155";
        }

        SoundSenderDemo aDemo = new SoundSenderDemo(false);
        Participant p1 = new Participant(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[2]) + 1);
        //Participant p2 = new Participant("127.0.0.1", Integer.parseInt(args[2]), Integer.parseInt(args[2]) + 1);
        aDemo.rtpSession.addParticipant(p1);
        //aDemo.rtpSession.addParticipant(p2);
        aDemo.filename = args[0];
        aDemo.run();
        System.out.println("pktCount: " + pktCount);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void receiveData(DataFrame dummy1, Participant dummy2) {
        // We don't expect any data.
    }

    public void userEvent(int type, Participant[] participant) {
        //Do nothing
    }

    public int frameSize(int payloadType) {
        return 1;
    }

    public void run() {
        if (RTPSession.rtpDebugLevel > 1) {
            System.out.println("-> Run()");
        }
        File soundFile = new File(filename);
        if (!soundFile.exists()) {
            System.err.println("Wave file not found: " + filename);
            return;
        }

        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (UnsupportedAudioFileException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        //AudioFormat format = audioInputStream.getFormat();
        AudioFormat.Encoding encoding = new AudioFormat.Encoding("PCM_SIGNED");
        AudioFormat format = new AudioFormat(
                encoding,
                44100.0f,
                16,
                2,
                4,
                44100.0f,
                false
        );
        System.out.println(format.toString());

        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        long start = System.currentTimeMillis();
        int i = 1;
        try {
            while (nBytesRead != -1 && pktCount < 200) {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);

                if (nBytesRead >= 0) {
                    System.out.println(i);
                    System.out.println(abData.length);
                    i++;
                    rtpSession.sendData(abData);
                    //if(!this.local) {
                    //auline.write(abData, 0, abData.length);

                    pktCount++;
                }
                if (pktCount == 100) {
                    Enumeration<Participant> iter = this.rtpSession.getParticipants();
                    //System.out.println("iter " + iter.hasMoreElements());
                    Participant p = null;

                    while (iter.hasMoreElements()) {
                        p = iter.nextElement();

                        String name = "name";
                        byte[] nameBytes = name.getBytes();
                        String data = "abcd";
                        byte[] dataBytes = data.getBytes();


                        int ret = rtpSession.sendRTCPAppPacket(p.getSSRC(), 0, nameBytes, dataBytes);
                        System.out.println("!!!!!!!!!!!! ADDED APPLICATION SPECIFIC " + ret);
                        continue;
                    }
                    if (p == null)
                        System.out.println("No participant with SSRC available :(");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000 + " s");

        try {
            Thread.sleep(200);
        } catch (Exception e) {
        }

        this.rtpSession.endSession();

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        if (RTPSession.rtpDebugLevel > 1) {
            System.out.println("<- Run()");
        }
    }

    }
