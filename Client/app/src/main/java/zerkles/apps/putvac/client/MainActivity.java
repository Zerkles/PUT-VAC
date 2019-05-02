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

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    static TcpClient tcpClient;
    Button btn_send, btn_connect, btn_disconnect, btn_camera;
    static EditText ed_txt, ed_txt2, ed_txt3;
    public static TextView tv_txt;
    AsyncTask actualConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_send = findViewById(R.id.btn_send);
        btn_connect = findViewById(R.id.btn_connect);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_camera = findViewById(R.id.btn_camera);
        ed_txt = findViewById(R.id.ed_txt);
        //ed_txt2 = (EditText) findViewById(R.id.ed_txt2);
        ed_txt3 = findViewById(R.id.ed_txt3);
        tv_txt = findViewById(R.id.tv_txt);
        tv_txt.setText("Connected: NO");

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sends the message to the server
                if (tcpClient != null && ed_txt3.getText() != null) {
                    String text = ed_txt3.getText().toString();
                    if (!text.isEmpty()) {
                        new SendTask().execute(text);
                    }
                }
            }
        });


        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tcpClient != null) {
                    new ReconnectTask().execute("");
                } else {
                    new ConnectTask().execute("");
                }
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
                new DisconnectTask().execute("");
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    4);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(tcpClient!=null){
            tcpClient.update_conn_status();
        }

    }


    // -------- getter methods

    public static String getInputIP() {
        if (ed_txt.getText() != null) {
            return (ed_txt.getText().toString());
        } else {
            return null;
        }
    }

    public static String getPort() {
        return HttpClient.sendRequest("GET", getInputIP(), "/VAC/connect");
    }


    // -------- task classes

    public static class ConnectTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... message) {
            //we create a TCPClient object
            tcpClient = new TcpClient();

            String port = getPort();
            if (getInputIP() != null) {
                tcpClient.SERVER_IP = getInputIP();
            }
            if (port != null) {
                tcpClient.SERVER_PORT = Integer.parseInt(port);
            }
            tcpClient.connect();

            return null;
        }
    }

    public static class DisconnectTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... message) {
            if (tcpClient != null) {
                tcpClient.disconnect();
            }
            tcpClient = null;
            return null;
        }
    }

    public static class ReconnectTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... message) {
            if (tcpClient != null) {
                if (!Objects.equals(getInputIP(), tcpClient.SERVER_IP)) {
                    if (tcpClient != null) {
                        tcpClient.disconnect();
                    }

                    new ConnectTask().execute("");
                }
            }
            return null;
        }
    }

    public static class SendTask extends AsyncTask<String, byte[], byte[]> {
        @Override
        protected byte[] doInBackground(String... strings) {
            tcpClient.sendNumber(strings[0].length());
            tcpClient.sendString(strings[0]);
            return null;
        }
    }


    // -------- activity methods

    public void startCameraActivity() {
        if (tcpClient == null || !tcpClient.socket.isConnected()) {
            Toast.makeText(MainActivity.this, "Unable to open CameraActivity!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
        }
    }
}



