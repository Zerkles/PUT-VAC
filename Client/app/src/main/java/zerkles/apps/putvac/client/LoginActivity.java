package zerkles.apps.putvac.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    Button btn_register, btn_login;
    EditText ed_IP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        ed_IP = findViewById(R.id.ed_IP);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ed_IP.getText().toString().isEmpty()) {
                    startMenuActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Please type IP address first!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ed_IP.getText().toString().isEmpty()) {
                    startRegisterActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Please type IP address first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startMenuActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        MenuActivity.addr_ip = ed_IP.getText().toString();
    }

    public void startRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
