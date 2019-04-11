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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    static TcpClient mTcpClient;
    Button btn_send, btn_connect, btn_disconnect, btn_camera;
    static EditText ed_txt, ed_txt2, ed_txt3;
    TextView tv_txt, tv_txt2;
    AsyncTask actualConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_send = (Button) findViewById(R.id.btn_send);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        btn_camera = (Button) findViewById(R.id.btn_camera);
        ed_txt = (EditText) findViewById(R.id.ed_txt);
        //ed_txt2 = (EditText) findViewById(R.id.ed_txt2);
        ed_txt3 = (EditText) findViewById(R.id.ed_txt3);
        tv_txt = (TextView) findViewById(R.id.tv_txt);
        tv_txt2 = (TextView) findViewById(R.id.tv_txt2);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sends the message to the server
                if (mTcpClient != null && ed_txt3.getText()!=null) {
                    String text=ed_txt3.getText().toString();
                    mTcpClient.sendMessage(String.valueOf(text.length()));
                    mTcpClient.sendMessage(text);
                }
            }
        });


        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (actualConnection == null) {
                    actualConnection = new ConnectTask().execute("");
                } else {
                    actualConnection.cancel(true);
                    actualConnection = new ConnectTask().execute("");
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
                if (mTcpClient != null) {
                    mTcpClient.stopClient();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    4);
        }
        else {}
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d("STATE", "onRESUME");

        if (actualConnection != null && mTcpClient != null) {
            if (mTcpClient.socket.isConnected()) {
                tv_txt.setText(" Connected: YES");
            } else {
                tv_txt.setText(" Connected: NO");
            }

            tv_txt2.setText(" Connection Addres: " + mTcpClient.socket.getInetAddress());
        }
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });

            String port=getPort();
            if (getInputIP() != null) {
                mTcpClient.SERVER_IP = getInputIP();
            }
            if (port != null) {
                mTcpClient.SERVER_PORT = Integer.parseInt(port);
            }
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response: " + values[0]);
            //process server response here....

        }

        public String getInputIP() {
            if (MainActivity.ed_txt.getText() != null) {
                return (MainActivity.ed_txt.getText().toString());
            } else return null;

        }

        String getPort(){
            try{
                URL url = new URL("http://"+getInputIP()+"/VAC/connect");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Tutaj wysyła zapytanie
                int responseCode = conn.getResponseCode();

                // Tutaj sprawdzamy czy sukces
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    // Tutaj w odpowiedzi będzie port socket'u
                    while ((inputLine = in.readLine()) != null) {
                        // inputLine to port socket'u (trzeba przerobić ze string na int)
                        if(inputLine != null) {
                            Log.d("PORT:", inputLine);
                            return inputLine;
                        }
                    }
                }
            }
            catch(Exception e){Log.d("HTTP_CONNECTION",e.toString()+"ERR");}
        return null;
        }
    }



    public void startCameraActivity() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

//    public Integer getInputPort() {
//        if (ed_txt2.getText() != null) {
//            return Integer.parseInt(ed_txt2.getText().toString());
//        } else return null;
//
//    }

    public String getInputIP() {
        if (ed_txt.getText() != null) {
            return (ed_txt.getText().toString());
        } else return null;

    }
}



