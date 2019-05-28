package zerkles.apps.putvac.client;

import zerkles.apps.putvac.client.jlibrtp.*;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.net.DatagramSocket;

public class RtpClient implements RTPAppIntf {
    private RTPSession rtpSession;
    private int rtpPort;
    private int rtcpPort;
    private AudioTrack at;

    public synchronized void receiveData(DataFrame frame, Participant p) {
        if (at != null) {
            byte[] data = frame.getConcatenatedData();
            Log.d("RtpClient_receiveData", String.valueOf(data.length));
            at.write(data, 0, data.length);
        }
    }

    public void userEvent(int type, Participant[] participant) {
        //Do nothing
    }

    public int frameSize(int payloadType) {
        return 1;
    }

    public RtpClient(int rtpPort_, int rtcpPort_) {
        rtpPort = rtpPort_;
        rtcpPort = rtcpPort_;

        DatagramSocket rtpSocket = null;
        DatagramSocket rtcpSocket = null;

        try {
            rtpSocket = new DatagramSocket(rtpPort);
            rtcpSocket = new DatagramSocket(rtcpPort);
        } catch (Exception e) {
            Log.d("RtpClient_CONSTRUCTOR", "RTPSession failed to obtain port");
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
        at.pause();

        rtpSession = new RTPSession(rtpSocket, rtcpSocket);
        rtpSession.naivePktReception(true);
        rtpSession.RTPSessionRegister(this, null, null);
    }

    public void disconnect() {
        rtpSession.endSession();
    }

    public int getPort() {
        return rtpPort;
    }

    public void TogglePause(){
        if(at.getPlayState()== AudioTrack.PLAYSTATE_PAUSED){
            at.play();
        }
        else{
            at.pause();
        }
    }

    public RTPSession getSession() {
        return rtpSession;
    }
}
