package zerkles.apps.putvac.client;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Thread.sleep;

public class LoginActivity extends AppCompatActivity {
    Button btn_register, btn_login, btn_delete;
    static EditText ed_IP, ed_login, ed_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        btn_delete = findViewById(R.id.btn_delete);
        ed_IP = findViewById(R.id.ed_IP);
        ed_login = findViewById(R.id.ed_login);
        ed_password = findViewById(R.id.ed_password);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!getIP().isEmpty() && !getLogin().isEmpty() && !getPassword().isEmpty()) {
                    if (getLogin().equals("test") && getPassword().equals("test")) {
                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(intent);
                    } else {
                        new ConnectTask().execute(getLogin(), getPassword());
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill all gapes correctly!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!getIP().isEmpty()) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Please type in IP addres!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!getIP().isEmpty() && !getLogin().isEmpty() && !getPassword().isEmpty()) {
                    new DeleteTask().execute(getLogin(), getPassword());
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill all gapes correctly!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static String getIP() {
        return ed_IP.getText().toString();
    }

    public static String getLogin() {
        return ed_login.getText().toString();
    }

    public static String getPassword() {
        return ed_password.getText().toString();
    }

    public class ConnectTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
//            JSONObject passy = new JSONObject();
//            try {
//                passy.put("login", strings[0]);
//                passy.put("passwd", strings[1]);
//            } catch (
//                    JSONException e) {
//                e.printStackTrace();
//            }

            String login="login="+strings[0],passwd="&passwd="+strings[1];
            String os="&os="+Build.VERSION.RELEASE,brand="&brand="+Build.BRAND,model="&model="+Build.MODEL;
            //Log.d("OSversion", Build.VERSION.RELEASE);
            //Log.d("DeviceName", Build.BRAND + " " + Build.MODEL);

            String response = HttpClient.sendRequest("GET", getIP(), "/VAC/connect?"+login+passwd+os+brand+model);

            if (response != null && response.equals("401")) {
                //Toast.makeText(LoginActivity.this, "Authorization failure!", Toast.LENGTH_SHORT).show();
            } else if (response != null) {
                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                startActivity(intent);

                try {
                    sleep(1000);
                    JSONObject config = new JSONObject(response);
                    new MenuActivity.ConnectionTasks().execute("TCP", config.getString("tcp_port"));
                    new MenuActivity.ConnectionTasks().execute("RTP", config.getString("rtp_port"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            else{
                Log.d("Login","HTTP response is null");
            }

            return null;
        }
    }

    public class DeleteTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            JSONObject passy = new JSONObject();
            try {
                passy.put("login", strings[0]);
                passy.put("passwd", strings[1]);
            } catch (
                    JSONException e) {
                e.printStackTrace();
            }

            String response = HttpClient.sendRequest("DELETE", getIP(), "/VAC/db/Users/", passy);

            if (response != null && response.equals("401")) {
                Toast.makeText(LoginActivity.this, "Authorization failure!", Toast.LENGTH_SHORT).show();
            }

            return null;
        }
    }
}
