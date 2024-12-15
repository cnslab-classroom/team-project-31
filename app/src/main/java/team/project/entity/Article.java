package team.project.entity;

public class Article {
    public String enterprise;
    public String headline;
    public String contents;
    public String url;

    public Article(
        String enterprise,
        String headline,
        String contents,
        String url
    ) {
        this.enterprise = enterprise;
        this.headline = headline;
        this.contents = contents;
        this.url = url;
    }
}