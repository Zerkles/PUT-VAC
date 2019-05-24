package zerkles.apps.putvac.client;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    static TcpClient tcpClient;
    static ReceiverRTP rtpClient;
    Button btn_send, btn_connect, btn_disconnect, btn_camera;
    static EditText ed_txt, ed_txt2;
    static TextView tv_tcp, tv_rtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_send = findViewById(R.id.btn_send);
        btn_connect = findViewById(R.id.btn_connect);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_camera = findViewById(R.id.btn_camera);
        ed_txt = findViewById(R.id.ed_txt);
        ed_txt2 = findViewById(R.id.ed_txt2);
        tv_tcp = findViewById(R.id.tv_tcp);
        tv_rtp = findViewById(R.id.tv_rtp);

        checkPermissions();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sends the message through TCP to the server
                if (tcpClient != null && ed_txt2.getText() != null) {
                    String text = ed_txt2.getText().toString();
                    if (!text.isEmpty()) {
                        new SendTask().execute(text);
                    }
                }
            }
        });

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ConnectionTasks().execute("CONNECT");
                new ConnectionTasks().execute("RTP");
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity();
            }
        });

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ConnectionTasks().execute("DISCONNECT");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateConnectionStatus(tcpClient, rtpClient, tv_rtp, tv_tcp);
    }


    // -------- getter methods

    public static String getIP() {
        if (ed_txt.getText() != null) {
            return (ed_txt.getText().toString());
        }
        return null;
    }

    public static String getPort(String protocol) {
        if (protocol.equals("TCP")) {
            return HttpClient.sendRequest("GET", getIP(), "/VAC/TCP");
        } else if (protocol.equals("RTP")) {
            return HttpClient.sendRequest("GET", getIP(), "/VAC/RTP");
        }
        return null;
    }


    // -------- task classes

    public static class ConnectionTasks extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {

            switch (strings[0]) {
                case "CONNECT": {
                    if (tcpClient != null) {
                        tcpClient.disconnect();
                        tcpClient = null;
                    }

                    tcpClient = new TcpClient();

                    String PortTCP_S = getPort("TCP");
                    if (getIP() != null && PortTCP_S != null) {
                        tcpClient.SERVER_IP = getIP();
                        tcpClient.SERVER_PORT = Integer.parseInt(PortTCP_S);
                        tcpClient.connect();
                    }

                }
                break;
                case "DISCONNECT": {
                    if (tcpClient != null) {
                        tcpClient.disconnect();
                        tcpClient = null;
                    }
                    if (rtpClient != null) {
                        rtpClient.disconnect();
                        rtpClient = null;
                    }

                }
                break;
                case "RTP": {
                    if (rtpClient != null) {
                        rtpClient.disconnect();
                        rtpClient = null;
                    }

                    String PortRTP_S = getPort("RTP");
                    if (PortRTP_S != null) {
                        int port = Integer.parseInt(PortRTP_S);
                        rtpClient = new ReceiverRTP(port, port + 1);
                    }

                }
                break;
                default: {
                    Log.d("ConnectionTasks", "Wrong Task!");
                }
            }

            updateConnectionStatus(tcpClient, rtpClient, tv_rtp, tv_tcp);

            return null;
        }
    }

    public static class SendTask extends AsyncTask<String, byte[], byte[]> {
        @Override
        protected byte[] doInBackground(String... strings) {
            if (tcpClient != null) {
                tcpClient.sendNumber(strings[0].length());
                tcpClient.sendString(strings[0]);
            }
            return null;
        }
    }

    void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }
    }

    static void updateConnectionStatus(TcpClient tcpClient, ReceiverRTP rtpClient, TextView tv_rtp, TextView tv_tcp) {
        if (tcpClient == null || tcpClient.getSocket() == null || !tcpClient.getSocket().isConnected()) {
            Log.d("tcpClient_upConnStatus", "NOT Connected!");
            tv_tcp.setText("TCP Connection: NONE");
        } else {
            Log.d("tcpClient_upConnStatus", "Connected!");
            tv_tcp.setText("TCP Connection: " + tcpClient.getSocket().getInetAddress().getHostAddress() + ':' + tcpClient.getSocket().getPort());
        }

        if (rtpClient == null || rtpClient.getSession() == null) {
            Log.d("rtpClient_upConnStatus", "NOT Connected!");
            tv_rtp.setText("RTP Port: NONE");
        } else {
            Log.d("rtpClient_upConnStatus", "Connected!");
            tv_rtp.setText("RTP Port: " + rtpClient.getPort());
        }
    }

    // -------- activity methods

    public void startCameraActivity() {
        if (tcpClient == null || !tcpClient.getSocket().isConnected() || rtpClient == null) {
            Toast.makeText(MainActivity.this, "Unable to open CameraActivity!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }
    }
}



