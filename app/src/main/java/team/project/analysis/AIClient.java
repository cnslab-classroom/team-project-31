package team.project.analysis;

import team.project.entity.Article;
import java.io.IOException;
import java.util.concurrent.Future;

public interface AIClient {
    public String execute(Article article) throws IOException;
    public Future<String> executeAsync(Article article);
}