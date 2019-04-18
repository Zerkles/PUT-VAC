package zerkles.apps.putvac.client;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
            Log.d("HTTP_CONNECTION", e.toString() + "ERR");
        }
        return result;
    }
}
