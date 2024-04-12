package queryx;

import io.quarkus.logging.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class QueryUtils {
    public static void urlPing(String targetUrl) {
        Log.info("Pinging URL: " + targetUrl);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode(); // Get the response code
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Ping Successful: Response code = " + responseCode);
            } else {
                System.out.println("Ping Failed: Response code = " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error during HTTP ping: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect(); // Disconnect the HttpURLConnection
            }
        }
    }

    public static void lambdaCheck(Optional<String> checkURL) {
        checkURL.ifPresent(QueryUtils::urlPing);
    }
}
