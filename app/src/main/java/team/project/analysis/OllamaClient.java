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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import team.project.entity.Article;
import org.json.JSONObject;


public class OllamaClient implements AIClient {

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

        public String makePrompt(Article article) {
            // JSON에 방해되는 문자 전처리
            String safeHeadline = escapeJsonString(article.headline);
            String safeContents = escapeJsonString(article.contents);
            
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

            this.prompt = "이전의 모든 지시를 잊어버리세요. 당신은 주식 추천 경험이 있는 금융 전문가입니다. 만약 기사의 헤드라인과 내용이 다음날 거래되는 주식 가격에 대해 확실히 좋은 소식이라면 1에 가까운 숫자로 답하세요. 확실히 나쁜 소식이라면 -1에 가까운 숫자로 답하세요. 불확실하거나 주식 가격에 미치는 영향에 대한 정보가 충분하지 않다면 0에 가까운 숫자로 답하세요. 즉, 뉴스 기사의 헤드라인과 내용을 읽고 다음날 주식 가격에 좋은 소식인지, 나쁜 소식인지, 불확실한 소식인지를 -1부터 1 사이의 실수로 응답하면 됩니다. 만약 여러 개의 기사 또는 기사 내용이 있다면, 각각 여러 숫자로 응답하는 대신 종합적으로 고려하여 하나의 숫자만으로 응답하세요. 설명이나 추가적인 텍스트 없이 숫자만으로 답하세요";
        }
    }
    public ExecutorService executor;
    public Configuration configuration;

    public OllamaClient(ExecutorService executor) {
        this.executor = executor;
        this.configuration = new Configuration();
    }

    public OllamaClient(
        Configuration configuration
    ) {
        this.configuration = configuration;
    }

    public String execute(Article article) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) configuration.url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = String.format(
            "{\"model\": \"%s\", \"prompt\":\"%s\", \"stream\": false}", 
            configuration.modelName, 
            configuration.makePrompt(article)
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

    // 비동기 작업을 위한 Callable 생성
    private Callable<String> createAsyncTask(Article article) {
        return () -> execute(article);
    }

    // 비동기 호출 메서드
    public Future<String> executeAsync(Article article) {
        Callable<String> task = createAsyncTask(article);
        return executor.submit(task);
    }


}