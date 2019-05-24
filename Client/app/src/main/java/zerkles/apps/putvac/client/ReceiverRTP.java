package zerkles.apps.putvac.client;

import zerkles.apps.putvac.client.jlibrtp.*;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.net.DatagramSocket;

public class ReceiverRTP implements RTPAppIntf {
    private RTPSession rtpSession;
    private int rtpPort;
    private int rtcpPort;
    private AudioTrack at;

    public synchronized void receiveData(DataFrame frame, Participant p) {
        if (at != null) {
            byte[] data = frame.getConcatenatedData();
            Log.d("ReceiverRTP_receiveData", String.valueOf(data.length));
            at.write(data, 0, data.length);
        }
    }

    public void userEvent(int type, Participant[] participant) {
        //Do nothing
    }

    public int frameSize(int payloadType) {
        return 1;
    }

    public ReceiverRTP(int rtpPort_, int rtcpPort_) {
        rtpPort = rtpPort_;
        rtcpPort = rtcpPort_;

        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;

        try {
            rtpSocket = new DatagramSocket(rtpPort);
            rtcpSocket = new DatagramSocket(rtcpPort);
        } catch (Exception e) {
            Log.d("ReceiverRTP_CONSTRUCTOR", "RTPSession failed to obtain port");
            e.printStackTrace();
        }

        at = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                1024,
                AudioTrack.MODE_STREAM
        );
        at.play();

        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession.naivePktReception(true);
        rtpSession.RTPSessionRegister(this, null, null);
    }


    public void start() {
        Log.d("ReceiverRTP_START", "Setup");

        ReceiverRTP object = new ReceiverRTP(rtpPort, rtcpPort);

        object.doStuff();
        Log.d("ReceiverRTP_START", "Done");
    }

    private void doStuff() {
        Log.d("ReceiverRTP_doStuff", "-> ReceiverDemo.doStuff()");

        try {
            int nBytesRead = 0;
            while (nBytesRead != -1) {
                // Used to write audiot to auline here,
                // now moved directly to receiveData.
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        rtpSession.endSession();
    }

    public int getPort() {
        return rtpPort;
    }

    public RTPSession getSession() {
        return rtpSession;
    }
}
