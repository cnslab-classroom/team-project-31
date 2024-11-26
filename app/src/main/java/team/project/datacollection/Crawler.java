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

public class Crawler {

    // Inner class to store article information
    static class Article {
        String title;
        String content;
        String url;

        // Constructor
        public Article(String title, String content, String url) {
            this.title = title;
            this.content = content;
            this.url = url;
        }
    }

    // URL of the website to crawl
    String homeUrl = "https://finance.naver.com/news/mainnews.naver";

    // List to store articles
    List<Article> articleList = new ArrayList<Article>();
    JSONArray articlesArray = new JSONArray();
    

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

                // Save the article to the array
                Article article = new Article(title, newsContents.text(), realLink);
                articleList.add(article);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert the article list to JSON array
        for (Article article : articleList) {
            JSONObject articleJSON = new JSONObject();
            articleJSON.put("title", article.title);
            articleJSON.put("content", article.content);
            articleJSON.put("url", article.url);
            articlesArray.put(articleJSON);
        }

        return;
    }

    // Return the article list
    public JSONArray getArticles() {
        return articlesArray;
    }

    // Print the article list for Testing
    public void printArticleList() {

        for(Article article : articleList) {
            System.out.println("Title: " + article.title);
            System.out.println("Content: " + article.content);
            System.out.println("URL: " + article.url);
            System.out.println();
        }
    }

}

