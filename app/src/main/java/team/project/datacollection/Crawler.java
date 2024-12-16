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

    // URL of the website to crawl
    private final String homeUrl = "https://finance.naver.com/news/mainnews.naver";

    // List to store articles
    private List<Article> articleList = new ArrayList<Article>();
    private JSONArray articlesArray = new JSONArray();
    

    // main method to crawl the website
    public void crawl(){
        try {

            // Connect to the naver finance's main news and get the document
            // Set user agent to avoid 403 error
            Document doc = Jsoup.connect(homeUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36").get();

            // <li> elements with class "block1" contain the news
            Elements newsElements = doc.select("li.block1");

            // Loop through the news elements
            for (Element news : newsElements) {

                // Get the title and link of the news
                String title = news.select("a").text();
                String link = news.select("a").attr("href");

                // Check if the title or link is empty
                if (title.isEmpty() || link.isEmpty()) {
                    System.out.println("Title or link is empty.");
                    continue;
                }


                // Extract article ID and office ID from the link
                String regex = "article_id=(\\d+)&.*?office_id=(\\d+)";

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
            articleJSON.put("headline", article.headline);
            articleJSON.put("contents", article.contents);
            articleJSON.put("url", article.url);
            articlesArray.put(articleJSON);
        }

        return;
    }

    // Return the article list as a JSON array
    public JSONArray getArticlesJSON() {
        return articlesArray;
    }

    public List<Article> getArticles() {
        return articleList;
    }

    // Return the article list as a list of strings
    public List<String> getArticlesString() {
        List<String> articles = new ArrayList<String>();
        for(int i = 0; i < articlesArray.length(); i++) {
            articles.add(articlesArray.getJSONObject(i).toString());
        }
        return articles;
    }

    // Print the article list for Testing
    public void printArticleList() {

        for(int i = 0; i < articlesArray.length(); i++) {
            System.out.println(articlesArray.getJSONObject(i).toString() + "\n");
        }
    }

}

