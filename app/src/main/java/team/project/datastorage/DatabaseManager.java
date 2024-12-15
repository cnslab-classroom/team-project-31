package team.project.datastorage;

import team.project.entity.Article;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://pro.freedb.tech:3306/stockAnalysis";
    private static final String USER = "testUser";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (InputStream in = DatabaseManager.class.getResourceAsStream("init.sql");
             Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    // 분석 결과 저장
    public static void storeData(Article article, double estimateValue) {
        String sql = "INSERT INTO article_evaluations (headline, contents, url, estimate_value) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, article.headline);
            pstmt.setString(2, article.contents);
            pstmt.setString(3, article.url);
            pstmt.setDouble(4, estimateValue);
            pstmt.executeUpdate();
            
            System.out.println("Article data and estimate value stored successfully for URL: " + article.url);
        } catch (SQLException e) {
            System.err.println("Failed to store article data: " + e.getMessage());
        }
    }

    // 모든 기사 데이터 출력
    public static void printAllData() {
        String sql = "SELECT * FROM article_evaluations ORDER BY evaluation_date DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== 모든 기사 데이터 ===");
            System.out.println("=====================================");
            
            while (rs.next()) {
                System.out.println("Headline: " + rs.getString("headline"));
                System.out.println("Contents: " + rs.getString("contents"));
                System.out.println("URL: " + rs.getString("url"));
                System.out.println("Estimate Value: " + rs.getDouble("estimate_value"));
                System.out.println("Evaluation Date: " + rs.getTimestamp("evaluation_date"));
                System.out.println("-------------------------------------");
            }
            
        } catch (SQLException e) {
            System.err.println("Error printing all data: " + e.getMessage());
        }
    }

    // URL로 기사 검색
    public static Article searchData(String url) {
        String sql = "SELECT * FROM article_evaluations WHERE url = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, url);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Article(
                    rs.getString("headline"),
                    rs.getString("contents"),
                    rs.getString("url")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching data: " + e.getMessage());
        }
        
        return null;
    }

    // URL로 기사 데이터 출력
    public static void printData(String url) {
        Article article = searchData(url);
        
        if (article == null) {
            System.out.println("No article found for URL: " + url);
            return;
        }

        System.out.println("\n=== 기사 데이터 ===");
        System.out.println("-------------------");
        System.out.println("Headline: " + article.headline);
        System.out.println("Contents: " + article.contents);
        System.out.println("URL: " + article.url);
        System.out.println("-------------------");
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}