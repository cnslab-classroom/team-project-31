package team.project.datacollection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import team.project.entity.Article;

public class Crawler {

    // 네이버 증권 뉴스 홈 URL
    private final String URL_HOME = "https://finance.naver.com/news/mainnews.naver";

    // 403 에러를 피하기 위해 User-Agent 설정
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";
    
    // CSS Query
    private final String CQ_MAIN_NEWS_LIST = "li.block1";
    private final String CQ_POPULAR_SEARCH_TABLE = "table.type_r1";
    private final String CQ_POPULAR_SEARCH_LIST = "tr";
    private final String CQ_POPULAR_SEARCH_NEWS_SECTION = "div.sub_section.news_section";
    private final String CQ_POPULAR_SEARCH_NEWS_LIST = "li";
    private final String CQ_HEADLINE = "h2.media_end_head_headline";
    private final String CQ_CONTENTS = "article#dic_area";
    
    // article_id와 office_id 추출을 위한 정규식
    private final String ARTICLE_OFFICE_ID = "article_id=(\\d+)&.*?office_id=(\\d+)";

    // Article을 저장할 리스트와 JSON 배열
    private List<Article> articleList;
    private JSONArray articleJSONArray;

    public Crawler() {
        articleList = new ArrayList<Article>();
        articleJSONArray = new JSONArray();
    }
    

    // 메인 뉴스를 크롤링하고 articleList와 articlesArray에 저장하는는 메서드
    public void crawl(){
        try {
            // Connect to the naver finance's main news and get the document
            // Set user agent to avoid 403 error
            Document doc = Jsoup.connect(URL_HOME).userAgent(USER_AGENT).get();

            // <li> elements with class "block1" contain the news
            Elements newsElements = doc.select(CQ_MAIN_NEWS_LIST);

            // Loop through the news elements
            for (Element news : newsElements) {

                // Get the title and link of the news
                String title = news.select("a").text();
                String link = news.select("a").attr("href");

                // Extract article ID and office ID from the link
                String regex = ARTICLE_OFFICE_ID;

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(link);
        
                String realLink = "";

                if (matcher.find()) {

                    String articleId = matcher.group(1);
                    String officeId = matcher.group(2);

                    realLink = "https://n.news.naver.com/mnews/article/" + officeId + "/" + articleId ;

                } else {
                    System.out.println("Cannot find article ID and office ID.");
                }
                
                // Connect to the article page and get the document
                Document newsDoc = Jsoup.connect(realLink).get();
                Elements newsContents = newsDoc.select("article#dic_area");

                // Classify the enterprise of the article
                EnterpriseClassifier classifier = new EnterpriseClassifier();
                String enterprise = classifier.execute(title, newsContents.text());
                
                // Save the article to the array
                Article article = new Article(enterprise, title, newsContents.text(), realLink);
                articleList.add(article);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert the article list to JSON array
        for (Article article : articleList) {
            JSONObject articleJSON = new JSONObject();
            articleJSON.put("enterprise", article.enterprise);
            articleJSON.put("headline", article.headline);
            articleJSON.put("contents", article.contents);
            articleJSON.put("url", article.url);
            articleJSONArray.put(articleJSON);
        }

        return;
    }

    public void crawlPopular(){
        try{
            // 네이버페이 증권 뉴스 홈
            Document doc = Jsoup.connect(URL_HOME).userAgent(USER_AGENT).get();
            
            // 인기검색어 테이블
            Elements elements = doc.select(CQ_POPULAR_SEARCH_TABLE).select(CQ_POPULAR_SEARCH_LIST);

            for(Element element : elements){
                // 각 인기검색어 기업과 링크
                String enterprise = element.select("a").text();
                String stockLink = "https://finance.naver.com" + element.select("a").attr("href");

                // 기업 정보 페이지로 이동
                Document popularDoc = Jsoup.connect(stockLink).userAgent(USER_AGENT).get();
                // 기업 정보 페이지의 뉴스 섹션
                Elements popularElements = popularDoc.select(CQ_POPULAR_SEARCH_NEWS_SECTION);
                popularElements = popularElements.select(CQ_POPULAR_SEARCH_NEWS_LIST);

                for(Element popularElement : popularElements){
                    // 뉴스 링크
                    String newsLink = popularElement.select("a").attr("href");

                    // 기사 ID 추출
                    String regex = ARTICLE_OFFICE_ID;

                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(newsLink);
    
                    if (matcher.find()) {
    
                        String articleId = matcher.group(1);
                        String officeId = matcher.group(2);
    
                        // 완성된 기사 링크
                        newsLink = "https://n.news.naver.com/mnews/article/" + officeId + "/" + articleId;
                        
                    } else {
                        System.out.println("Cannot find article ID and office ID.");
                    }

                    // 기사 페이지로 이동
                    Document newsDoc = Jsoup.connect(newsLink).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36").get();
                    // 기사 제목과 본문 추출
                    String title = newsDoc.select(CQ_HEADLINE).text();
                    String contents = newsDoc.select(CQ_CONTENTS).text();

                    // 기사 저장
                    Article article = new Article(enterprise, title, contents, newsLink);
                    // 리스트에 추가
                    articleList.add(article);
                }

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Convert the article list to JSON array
        for (Article article : articleList) {
            JSONObject articleJSON = new JSONObject();
            articleJSON.put("enterprise", article.enterprise);
            articleJSON.put("headline", article.headline);
            articleJSON.put("contents", article.contents);
            articleJSON.put("url", article.url);
            articleJSONArray.put(articleJSON);
        }

        return;
    }

    // 추출된 Article들을 JSONArray로 반환 
    public JSONArray getArticlesJSON() {
        return articleJSONArray;
    }
    // 추출된 Article들을 List<Article>로 반환
    public List<Article> getArticles() {
        return articleList;
    }

}

