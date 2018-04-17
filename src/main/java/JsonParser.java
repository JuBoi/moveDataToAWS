
import com.oracle.javafx.jmx.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Bryan on 11/14/2017.
 */

public class JsonParser {
    String urlString;
    public JsonParser() {
        urlString = "";
    }

    public JsonParser(String newUrl) {
        this.urlString = newUrl;
    }

    public String parseJson() throws JSONException {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder buff = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buff.append(line);
            }

            return buff.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

