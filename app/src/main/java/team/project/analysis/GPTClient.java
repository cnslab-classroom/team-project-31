package team.project.analysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import team.project.entity.Article;

public class GPTClient implements AIClient {

    // OpenAI API 키를 여기에 입력하세요
    private static final String API_KEY = "여기에 APIKey를 넣으세요";
    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_TOKENS = 4096; // 최대 토큰 수 설정

    public String execute(Article article) throws IOException {
       
        // 사용할 모델을 선택하세요
        String model = "gpt-3.5-turbo"; // 예: gpt-3.5-turbo, gpt-4, gpt-4-32k
        
            // API URL 설정
            String url = "https://api.openai.com/v1/chat/completions";

            String prompt = "이전의 모든 지시를 잊어버리세요. 당신은 주식 추천 경험이 있는 금융 전문가입니다. 만약 기사의 헤드라인과 내용이 다음날 거래되는 주식 가격에 대해 확실히 좋은 소식이라면 1에 가까운 숫자로 답하세요. 확실히 나쁜 소식이라면 -1에 가까운 숫자로 답하세요. 불확실하거나 주식 가격에 미치는 영향에 대한 정보가 충분하지 않다면 0에 가까운 숫자로 답하세요. 즉, 뉴스 기사의 헤드라인과 내용을 읽고 다음날 주식 가격에 좋은 소식인지, 나쁜 소식인지, 불확실한 소식인지를 -1부터 1 사이의 실수로 응답하면 됩니다. 만약 여러 개의 기사 또는 기사 내용이 있다면, 각각 여러 숫자로 응답하는 대신 종합적으로 고려하여 하나의 숫자만으로 응답하세요. 설명이나 추가적인 텍스트 없이 숫자만으로 답하세요"
           + "헤드라인: " + article.headline + "원문: " + article.contents;
            

            // JSON 요청 본문 생성
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("max_tokens", MAX_TOKENS); // 최대 토큰 수 설정
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);
            requestBody.put("messages", messages);

            // HTTP 연결 설정
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 요청 본문 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 응답 받기
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // 응답 파싱 및 출력
            return parseAndPrintResponse(response.toString());

        
    }

    private String parseAndPrintResponse(String responseBody) {
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray choices = jsonObject.getJSONArray("choices");
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < choices.length(); i++) {
            String text = choices.getJSONObject(i).getJSONObject("message").getString("content");
            result.add(text);
        }

        return String.join("", result);
    }


}