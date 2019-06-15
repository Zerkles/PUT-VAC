package zerkles.apps.putvac.client;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static zerkles.apps.putvac.client.LoginActivity.getIP;

public class DataBaseActivity extends AppCompatActivity {

    TextView tv_queryName;
    LinearLayout LinearScrollLayout, LineaerColumnsLayout;
    Button btn_loginHistory, btn_sessionHistory, btn_dataAmount, btn_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        tv_queryName = findViewById(R.id.tv_queryName);
        LinearScrollLayout = findViewById(R.id.MainVerticalLine);
        LineaerColumnsLayout = findViewById(R.id.ColumnsLine);

        btn_loginHistory = findViewById(R.id.btn_loginHistory);
        btn_sessionHistory = findViewById(R.id.btn_sessionHistory);
        btn_dataAmount = findViewById(R.id.btn_dataAmount);
        btn_test = findViewById(R.id.btn_test);

        btn_loginHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_queryName.setText("Login History");
                tv_queryName.setTextSize(30);
                LinearScrollLayout.removeAllViews();
                LineaerColumnsLayout.removeAllViews();
                new HTTPTask().execute("login_history");
            }
        });

        btn_sessionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_queryName.setText("Session History");
                tv_queryName.setTextSize(30);
                LinearScrollLayout.removeAllViews();
                LineaerColumnsLayout.removeAllViews();
                new HTTPTask().execute("session_history");
            }
        });

        btn_dataAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_queryName.setText("Data Amount");
                tv_queryName.setTextSize(30);
                LinearScrollLayout.removeAllViews();
                LineaerColumnsLayout.removeAllViews();
                new HTTPTask().execute("data_amount");
            }
        });

        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_queryName.setText("Test");
                tv_queryName.setTextSize(30);
                LinearScrollLayout.removeAllViews();
                LineaerColumnsLayout.removeAllViews();
                new HTTPTask().execute("test");
            }
        });
    }

    private void updateScrollView(String responseHTTP) {
        LinearLayout.LayoutParams row_layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableLayout.LayoutParams cell_layout = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f);

        if (LinearScrollLayout.getChildCount() != 0 && LineaerColumnsLayout.getChildCount() != 0) {
            LinearScrollLayout.removeAllViews();
            LineaerColumnsLayout.removeAllViews();
        }

        //Log.d("JSON", responseHTTP);

        try {
            JSONObject json = new JSONObject(responseHTTP);
            JSONArray row = json.getJSONArray(json.names().getString(0));
            for (int j = 0; j < row.length(); j++) {
                //Log.d("columns names",row.getString(j));

                TextView tv_cell = new TextView(this);
                tv_cell.setLayoutParams(cell_layout);
                tv_cell.setBackground(getDrawable(R.drawable.border));
                tv_cell.setText(row.getString(j));
                LineaerColumnsLayout.addView(tv_cell);
            }

            for (int i = 1; i < json.length(); i++) {
                row = json.getJSONArray(json.names().getString(i));
                //Log.d("row",row.toString());

                LinearLayout line = new LinearLayout(this); /// horizontal by default
                line.setLayoutParams(row_layout);


                for (int j = 0; j < row.length(); j++) {
                    TextView tv_cell = new TextView(this);
                    tv_cell.setLayoutParams(cell_layout);
                    tv_cell.setBackground(getDrawable(R.drawable.border));
                    tv_cell.setText(row.getString(j));
                    line.addView(tv_cell);
                }
                LinearScrollLayout.addView(line);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public class HTTPTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String login = "login=" + LoginActivity.getLogin();
            String passwd = "&passwd=" + LoginActivity.getPassword();
            HttpResponse response;

            if(!strings[0].equals("test")) {
                response = HttpClient.sendRequest("GET", getIP(), "/VAC/db/Statistics?" + login + passwd + "&type=" + strings[0]);
            }
            else{
                response = HttpClient.sendRequest("GET", getIP(), "/VAC/db/Test_Table");
            }
            publishProgress(response.data);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            updateScrollView(values[0]);
        }
    }
}
