package team.project.analysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.StringBuilder;

import org.json.JSONObject;


public class OllamaClient {

    public class Configuration {
        public String modelName;
        public URL url;

        public Configuration() {
            this.modelName = "llama3.2";
            try {
                this.url = new URL("http://localhost:11434/api/generate");
            } catch (MalformedURLException e){
                this.url = null;
            }

        }
    }

    public Configuration configuration;

    public OllamaClient() {
        this.configuration = new Configuration();
    }

    public OllamaClient(
        Configuration configuration
    ) {
        this.configuration = configuration;
    }

    public String execute(String prompt) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) configuration.url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = String.format(
            "{\"model\": \"%s\", \"prompt\":\"%s\", \"stream\": false}", configuration.modelName, prompt
        );

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code  = conn.getResponseCode();
        System.out.println("ResponseCode: " + code) ;

        BufferedReader in = new BufferedReader (new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in. readLine()) != null) {
        response. append (line);
        }
        in.close();
        // Print the response
        System.out.println("Response Body: " + response.toString());
        // Parse the JSON response and print the "response" field
        JSONObject jsonResponse = new JSONObject(response.toString());
        String responseText = jsonResponse.getString("response");
        System.out.println("Response: " + responseText) ;
        // Close the connection conn Misconnect () ;
        conn.disconnect();

        return responseText;
    }
}