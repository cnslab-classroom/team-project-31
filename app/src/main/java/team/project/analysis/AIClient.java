package team.project.analysis;

import team.project.entity.Article;
import java.io.IOException;

public interface AIClient {
    public String execute(Article article) throws IOException;
}