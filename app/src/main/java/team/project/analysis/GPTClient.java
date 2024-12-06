package team.project.analysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class GPTClient {

    // OpenAI API 키를 여기에 입력하세요
    private static final String API_KEY = "여기에 APIKey를 넣으세요";
    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_TOKENS = 4096; // 최대 토큰 수 설정

    public void execute() {
        // 사용할 모델을 선택하세요
        String model = "gpt-3.5-turbo"; // 예: gpt-3.5-turbo, gpt-4, gpt-4-32k

        //선택 가능한 ChatGPT 모델:
        //"gpt-3.5-turbo": 고성능 모델 (유료) - 이미지 검색 기능 없음, 최대 토큰 수: 4096
        //"gpt-4": 최신 모델 (유료) - 이미지 검색 기능 없음, 최대 토큰 수: 8192
        //"gpt-4-32k": 확장된 최신 모델 (유료) - 이미지 검색 기능 없음, 최대 토큰 수: 32768
        //요금 정보: https://openai.com/pricing

        try {
            // API URL 설정
            String url = "https://api.openai.com/v1/chat/completions";

            // 멀티라인 입력 프롬프트
            // String prompt = "
            // 이전의 모든 지시를 잊어버리세요. 자신이 금융 자산 추천 경험이 있는 금융 전문가라고 생각하세요. 만약 기사의 헤드라인과 내용이 다음날 거래되는 암호화폐 가격에 대해 확실히 좋은 소식이라면 1에 가까운 숫자로 답하세요. 확실히 나쁜 소식이라면 -1에 가까운 숫자로 답하세요. 불확실하거나 암호화폐 가격에 미치는 영향에 대한 정보가 충분하지 않다면 0에 가까운 숫자로 답하세요. 즉, 뉴스 기사의 헤드라인과 내용을 읽고 다음날 암호화폐 가격에 좋은 소식인지, 나쁜 소식인지, 불확실한 소식인지를 -1부터 1 사이의 실수로 응답하면 됩니다. 만약 여러 개의 기사 또는 기사 내용이 있다면, 각각 여러 숫자로 응답하는 대신 종합적으로 고려하여 하나의 숫자만으로 응답하세요. 설명이나 추가적인 텍스트 없이 숫자만으로 답하세요
            // 헤드라인: 고려아연 210만원대까지 치솟아\u2026현대차 제치고 韓 시총 5위 [투자360],
            // 컨텐츠: [연합] [헤럴드경제=신동윤 기자] 경영권의 향방이 갈리는 임시주주총회를 앞두고 급등 중인 고려아연이 6일 장중 210만원 선을 넘어섰다. 이날 오전 9시 14분 현재 고려아연은 전 거래일 대비 7.50% 오른 215만원에 거래 중이다. 개장 직후 9% 가까이 올라 217만5000원에 거래되며 또다시 역대 최고가를 경신했다. 이로 인해 고려아연은 현대차를 제치고 시가총액 순위 5위에 오른 상태다. 고려아연은 전날 종가 200만원으로 역대 최고가를 경신하며 시총 6위에 올라섰는데, 고가에도 불구하고 이날도 큰 폭으로 상승하는 모습이다. 국내 증시에서 시가 200만원 이상인 주식이 거래된 것은 액면분할 전인 2017년 3월 삼성전자 주식이 마지막으로, 이후 7년 9개월 만에 200만원대 주식이 다시 탄생한 것이다. 고려아연 임시주주총회가 내년 1월 23일 열릴 예정인 가운데 장내 지분 매입 경쟁이 치열해지면서 주가가 고공행진 하는 것으로 풀이된다. 임시주총에서 권리 행사가 가능한 주주를 확정 짓는 주주명부 폐쇄일은 오는 20일로, 우호 지분을 확보할 수 있는 기간이 얼마 남지 않아 시장에서는 장내 지분 매집 경쟁이 더욱 치열해질 것이라는 관측이 나온다
            // ";

            String prompt = "안녕 나에게 인사해줘";

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
            parseAndPrintResponse(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseAndPrintResponse(String responseBody) {
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray choices = jsonObject.getJSONArray("choices");

        for (int i = 0; i < choices.length(); i++) {
            String text = choices.getJSONObject(i).getJSONObject("message").getString("content");
            printInChunks(text, BUFFER_SIZE);

            // Check for continuation token and fetch next response if available
            if (jsonObject.has("next")) {
                fetchNextResponse(jsonObject.getString("next"));
            }
        }
    }

    private void fetchNextResponse(String nextToken) {
        // Implementation for fetching next response using the continuation token
        try {
            // API URL 설정
            String url = "https://api.openai.com/v1/chat/completions";

            // JSON 요청 본문 생성
            JSONObject requestBody = new JSONObject();
            requestBody.put("next", nextToken);

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
            parseAndPrintResponse(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printInChunks(String text, int bufferSize) {
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(length, start + bufferSize);
            System.out.println(text.substring(start, end));
            start = end;
        }
    }
}