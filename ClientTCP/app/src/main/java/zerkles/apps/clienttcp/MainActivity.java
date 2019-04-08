package zerkles.apps.clienttcp;


import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    TcpClient mTcpClient;
    Button btn_send,btn_connect;
    EditText ed_txt,ed_txt2,ed_txt3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_send=(Button)findViewById(R.id.btn_send);
        btn_connect=(Button)findViewById(R.id.btn_connect);
        ed_txt=(EditText)findViewById(R.id.ed_txt);
        ed_txt2=(EditText)findViewById(R.id.ed_txt2);
        ed_txt3=(EditText)findViewById(R.id.ed_txt3);

        btn_send.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sends the message to the server
                if (mTcpClient != null) {
                    // URL do podłączenia się pod serwer (zmień adres na taki jaki ma być
                    // String address = "coś:80"
                    // URL url = new URL("http://" + address + "/VAC/connect");
                    try{
                        URL url = new URL("http://127.0.0.1:80/VAC/connect");
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
                                Log.d("x",inputLine);
                            }
                        }
                    }
                    catch(Exception e){}


                    TcpClient.SERVER_IP=ed_txt.getText().toString();
                    TcpClient.SERVER_PORT=Integer.parseInt(ed_txt2.getText().toString());
                    String text = ed_txt3.getText().toString();

                    mTcpClient.sendMessage(String.valueOf(text.length()));
                    mTcpClient.sendMessage(text);
                }
            }
        });


        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sends the message to the server
                    new ConnectTask().execute("");
            }
        });


        if (mTcpClient != null) {
            mTcpClient.stopClient();
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
    }
}

