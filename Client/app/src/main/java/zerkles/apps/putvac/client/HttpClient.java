package zerkles.apps.putvac.client;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


class HttpClient {
    static String sendRequest(String method, String serverAddr, String route) {
        String result = null;

        try {
            URL url = new URL("http://" + serverAddr + route);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);

            // Tutaj wysyła zapytanie
            int responseCode = conn.getResponseCode();

            // Tutaj sprawdzamy czy sukces
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                // Tutaj odczyt odpowiedzi
                result = in.readLine();
            }
        } catch (Exception e) {
            Log.d("httpClient_sendRequest", e.toString());
            e.printStackTrace();
        }
        return result;
    }
    static String sendRequest(String method, String serverAddr, String route, JSONObject content) {
        String result = null;

        try {
            URL url = new URL("http://" + serverAddr + route);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8;");
            OutputStream os = conn.getOutputStream();
            os.write(content.toString().getBytes("UTF-8"));
            os.close();

            // Tutaj wysyła zapytanie
            int responseCode = conn.getResponseCode();

            // Tutaj sprawdzamy czy sukces
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                // Tutaj odczyt odpowiedzi
                result = in.readLine();
            }
            else{
                result=String.valueOf(responseCode);
            }
        } catch (Exception e) {
            Log.d("httpClient_sendRequest", e.toString());
            e.printStackTrace();
        }
        return result;
    }

}
