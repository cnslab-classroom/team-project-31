package team.project.datacollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

public class EnterpriseClassifier {

    public EnterpriseClassifier() {
        this.configuration = new Configuration();
    }

    public class Configuration {
        public String modelName;
        public URL url;
        public String prompt;

        private String escapeJsonString(String input) {
            if (input == null) {
                return "";
            }
            // \n, \r, \t 등의 제어 문자와 큰따옴표, 백슬래시를 처리
            return input.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", " ")  // 개행문자는 공백으로 대체
                        .replace("\r", " ")  // 캐리지 리턴도 공백으로 대체
                        .replace("\t", " ");  // 탭 문자도 공백으로 대체
        }

        // 여기
        public String makePrompt(String headline, String contents) {
            // JSON에 방해되는 문자 전처리
            String safeHeadline = escapeJsonString(headline);
            String safeContents = escapeJsonString(contents);
            
            return prompt 
                + "헤드라인: " + safeHeadline
                + "원문: " + safeContents;
        }

        public Configuration() {
            this.modelName = "llama3.2";
            try {
                this.url = new URL("http://localhost:11434/api/generate");
            } catch (MalformedURLException e){
                this.url = null;
            }

            this.prompt = "이전의 모든 지시를 잊어버리세요. 당신에게 제공되는 것은 주식시장에 관한 기사의 제목과 내용입니다. 당신의 일은 기사에서 다루는 기업, 만약 기업에 대해 얘기하는게 아니라면 사업분야(제약, 유통, 화학, IT, 식료품, 비트코인 등), 그렇지 않으면 시장지수(코스피, 코스닥, 나스닥 등)중 하나로 말하세요. 문장이 아닌 오직 한 단어로 말하십시오. 예를 들면 '삼성전자' 혹은 '제약'이 될 수 있습니다. "; 
        }
    }
    public Configuration configuration;

    public EnterpriseClassifier(
        Configuration configuration
    ) {
        this.configuration = configuration;
    }

    // 여기
    public String execute(String newsHeadline, String newsContents) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) configuration.url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = String.format(
            "{\"model\": \"%s\", \"prompt\":\"%s\", \"stream\": false}", 
            configuration.modelName, 
            configuration.makePrompt(newsHeadline, newsContents)
        );

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code  = conn.getResponseCode();
        // System.out.println("ResponseCode: " + code) ;

        BufferedReader in;
        if (code >= 400) {
            // 에러 응답일 경우 에러 스트림을 읽음
            in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        } else {
            // 정상 응답일 경우 일반 스트림을 읽음
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in. readLine()) != null) {
            response.append(line);
        }
        in.close();
        
        if (code >= 400) {
            System.out.println("Response Body: " + response.toString());
            throw new IOException("HTTP Error Code: " + code + " - " + response.toString());
        } 
        
        JSONObject jsonResponse = new JSONObject(response.toString());
        String responseText = jsonResponse.getString("response");
        // System.out.println("Response: " + responseText) ;
        
        conn.disconnect();

        return responseText;
    }
    
}
