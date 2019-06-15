package zerkles.apps.putvac.client;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class MenuActivity extends AppCompatActivity {

    static TcpClient tcpClient;
    static RtpClient rtpClient;
    Button btn_connect, btn_disconnect, btn_camera, btn_db, btn_tcpTest;
    static TextView tv_tcp, tv_rtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btn_connect = findViewById(R.id.btn_connect);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_db = findViewById(R.id.btn_db);
        btn_tcpTest = findViewById(R.id.btn_tcpTest);
        btn_camera = findViewById(R.id.btn_camera);

        tv_tcp = findViewById(R.id.tv_tcp);
        tv_rtp = findViewById(R.id.tv_rtp);

        checkPermissions();

        btn_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, DataBaseActivity.class);
                startActivity(intent);
            }
        });

        btn_tcpTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, TcpTestActivity.class);
                startActivity(intent);
            }
        });

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ConnectionTasks().execute("CONNECT");
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tcpClient == null || !tcpClient.getSocket().isConnected() || rtpClient == null) {
                    Toast.makeText(MenuActivity.this, "Unable to open CameraActivity!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MenuActivity.this, CameraActivity.class);
                    startActivity(intent);
                }
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

    // -------- tasks

    public static class ConnectionTasks extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {

            switch (strings[0]) {
                case "CONNECT": {
                    String login = "login=" + LoginActivity.getLogin(), passwd = "&passwd=" + LoginActivity.getPassword();
                    HttpResponse response = HttpClient.sendRequest("GET", LoginActivity.getIP(), "/VAC/connect?" + login + passwd);

                    if (response != null) {
                        try {
                            JSONObject config = new JSONObject(response.data);
                            new ConnectionTasks().execute("TCP", config.getString("tcp_port"));
                            new ConnectionTasks().execute("RTP", config.getString("rtp_port"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
                case "TCP": {
                    if (tcpClient != null) {
                        tcpClient.disconnect();
                        tcpClient = null;
                    }

                    tcpClient = new TcpClient();

                    String PortTCP_S = strings[1];
                    if (LoginActivity.getIP() != null && PortTCP_S != null) {
                        tcpClient.SERVER_IP = LoginActivity.getIP();
                        tcpClient.SERVER_PORT = Integer.parseInt(PortTCP_S);
                        tcpClient.connect();
                    }

                }
                break;

                case "RTP": {
                    if (rtpClient != null) {
                        rtpClient.disconnect();
                        rtpClient = null;
                    }

                    String PortRTP_S = strings[1];
                    if (PortRTP_S != null) {
                        int port = Integer.parseInt(PortRTP_S);
                        rtpClient = new RtpClient(port, port + 1);
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
                tcpClient.sendString(strings[0]);
            }
            return null;
        }
    }

    // -------- permissions checkout

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Application cannot work without permissions", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    static void updateConnectionStatus(TcpClient tcpClient, RtpClient rtpClient, TextView tv_rtp, TextView tv_tcp) {
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
}



