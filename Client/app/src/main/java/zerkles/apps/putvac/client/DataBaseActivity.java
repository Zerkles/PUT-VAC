package zerkles.apps.putvac.client;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataBaseActivity extends AppCompatActivity {

    TextView tv_queryName;
    LinearLayout LinearScrollLayout, LineaerColumnsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        tv_queryName = findViewById(R.id.tv_queryName);
        LinearScrollLayout = findViewById(R.id.MainVerticalLine);
        LineaerColumnsLayout = findViewById(R.id.ColumnsLine);
        new HTTPTask().execute();

        Log.d("OSversion", Build.VERSION.RELEASE);
        Log.d("DeviceName", Build.BRAND + " " + Build.MODEL);
//        JSONArray names =json.names(); /// w kolumnach tylko rozmiar zewn petli moze byc zly
//
//        for(int i=0; i<json.length(); i++){
//            for(int j=0; j<names.length(); j++){
//                json.get(names[j])[i];
//            }
//
//        }
    }

    private void updateScrollView(String responseHTTP) {
        LinearLayout.LayoutParams row_layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableLayout.LayoutParams cell_layout = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f);

        if (LinearScrollLayout.getChildCount() != 0 && LineaerColumnsLayout.getChildCount() != 0) {
            LinearScrollLayout.removeAllViews();
            LineaerColumnsLayout.removeAllViews();
        }

        try {
            JSONObject json = new JSONObject(responseHTTP);
            JSONArray row = json.getJSONArray(json.names().getString(0));
            for (int j = 0; j < row.length(); j++) {
                System.out.print(row.getString(j));

                TextView tv_cell = new TextView(this);
                tv_cell.setLayoutParams(cell_layout);
                tv_cell.setBackground(getDrawable(R.drawable.border));
                tv_cell.setText(row.getString(j));
                LineaerColumnsLayout.addView(tv_cell);
            }

            for (int i = 1; i < json.length(); i++) {
                row = json.getJSONArray(json.names().getString(i));

                System.out.println(json.names());

                LinearLayout line = new LinearLayout(this); /// horizontal by default
                line.setLayoutParams(row_layout);


                for (int j = 0; j < row.length(); j++) {
                    System.out.print(row.getString(j));

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
            String response = HttpClient.sendRequest("GET", LoginActivity.getIP(), "/VAC/db/Statistics/");
            publishProgress(response);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            updateScrollView(values[0]);
        }
    }
}
