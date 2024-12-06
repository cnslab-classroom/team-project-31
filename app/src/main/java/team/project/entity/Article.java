package team.project.entity;

public class Article {
    public String headline;
    public String contents;
    public String url;

    public Article(
        String headline,
        String contents,
        String url
    ) {
        this.headline = headline;
        this.contents = contents;
        this.url = url;
    }
}