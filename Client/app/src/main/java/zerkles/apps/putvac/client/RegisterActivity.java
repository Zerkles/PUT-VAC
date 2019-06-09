package zerkles.apps.putvac.client;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    Button btn_register;
    EditText ed_login, ed_password, ed_passwordR;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btn_register = findViewById(R.id.btn_register);
        ed_login = findViewById(R.id.ed_login);
        ed_password = findViewById(R.id.ed_password);
        ed_passwordR = findViewById(R.id.ed_passwordR);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!getLogin().isEmpty() && !getPassword().isEmpty() && !getPasswordR().isEmpty()) {
                    if (getPassword().equals(getPasswordR())) {
                        new RegisterTask().execute(getLogin(), getPassword());
                    } else {
                        Toast.makeText(RegisterActivity.this, "Passwords are not equal!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Please fill all gapes correctly!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    String getLogin() {
        return ed_login.getText().toString();
    }

    String getPassword() {
        return ed_password.getText().toString();
    }

    String getPasswordR() {
        return ed_passwordR.getText().toString();
    }

    public class RegisterTask extends AsyncTask<String, String, String> {
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

            String response = HttpClient.sendRequest("POST", LoginActivity.getIP(), "/VAC/db/Users/", passy);

            if (response.equals("201")) {
                Toast.makeText(RegisterActivity.this, "Added new user!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            } else if (response.equals("400")) {
                Toast.makeText(RegisterActivity.this, "User already exist!", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //updateScrollView(values[0]);
        }
    }
}
