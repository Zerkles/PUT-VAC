package zerkles.apps.putvac.client;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
                if (!ed_IP.getText().toString().isEmpty() || !ed_login.getText().toString().isEmpty() || !ed_password.getText().toString().isEmpty()) {
                    new ConnectTask().execute(ed_login.getText().toString(), ed_password.getText().toString());
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill all gapes correctly!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ed_IP.getText().toString().isEmpty()) {
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
                if (!ed_IP.getText().toString().isEmpty() || !ed_login.getText().toString().isEmpty() || !ed_password.getText().toString().isEmpty()) {
                    new DeleteTask().execute(ed_login.getText().toString(),ed_password.getText().toString());
                }
                else {
                    Toast.makeText(LoginActivity.this, "Please fill all gapes correctly!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static String getIP() {
        return ed_IP.getText().toString();
    }

    public class ConnectTask extends AsyncTask<String, String, String> {
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

            String response = HttpClient.sendRequest("GET", getIP(), "/VAC/db/Users/", passy);

            if (response != null && response.equals("401")) {
                Toast.makeText(LoginActivity.this, "Authorization failure!", Toast.LENGTH_SHORT).show();
            }
            else if(response != null){
                try {
                    JSONObject config = new JSONObject(response);
                    new MenuActivity.ConnectionTasks().execute("TCP", config.getString("tcp_port"));
                    new MenuActivity.ConnectionTasks().execute("RTP", config.getString("rtp_port"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
