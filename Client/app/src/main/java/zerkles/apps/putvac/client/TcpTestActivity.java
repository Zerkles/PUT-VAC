package zerkles.apps.putvac.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static zerkles.apps.putvac.client.MenuActivity.tcpClient;

public class TcpTestActivity extends AppCompatActivity {

    Button btn_send;
    EditText ed_Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_test);

        ed_Text = findViewById(R.id.ed_Text);
        btn_send = findViewById(R.id.btn_send);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sends the message through TCP to the server
                if (tcpClient != null && ed_Text.getText() != null) {
                    String text = ed_Text.getText().toString();
                    if (!text.isEmpty()) {
                        new MenuActivity.SendTask().execute(text);
                    }
                }
            }
        });
    }
}
