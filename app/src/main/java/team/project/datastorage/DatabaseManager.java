package team.project.datastorage;

import team.project.entity.Article;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/stock_analysis";
    private static final String USER = "root";
    private static final String PASSWORD = "1q2w3e4r!";

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
    public static void storeData(String stockCode, Article article, double estimateValue) {
        String sql = "INSERT INTO stock_evaluations (stock_code, headline, contents, url, estimate_value) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, stockCode);
            pstmt.setString(2, article.headline);
            pstmt.setString(3, article.contents);
            pstmt.setString(4, article.url);
            pstmt.setDouble(5, estimateValue);
            pstmt.executeUpdate();
            
            System.out.println("Article data and estimate value stored successfully for stock code: " + stockCode);
        } catch (SQLException e) {
            System.err.println("Failed to store article data: " + e.getMessage());
        }
    }

    // 모든 종목 데이터 출력
    public static void printAllData() {
        String sql = "SELECT * FROM stock_evaluations ORDER BY evaluation_date DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== 모든 종목 데이터 ===");
            System.out.println("=====================================");
            
            while (rs.next()) {
                System.out.println("종목 코드: " + rs.getString("stock_code"));
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

    // 분석 결과 조회 - 특정 종목의 모든 기사 반환
    public static List<Article> searchData(String stockCode) {
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT * FROM stock_evaluations WHERE stock_code = ? ORDER BY evaluation_date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, stockCode);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Article article = new Article(
                    rs.getString("headline"),
                    rs.getString("contents"),
                    rs.getString("url")
                );
                articles.add(article);
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching data: " + e.getMessage());
        }
        
        return articles;
    }

    // 특정 종목 분석 결과 출력
    public static void printData(String stockCode) {
        List<Article> articles = searchData(stockCode);
        
        if (articles.isEmpty()) {
            System.out.println("No articles found for stock code: " + stockCode);
            return;
        }

        System.out.println("Articles for stock code: " + stockCode);
        System.out.println("-------------------");
        
        for (Article article : articles) {
            System.out.println("Headline: " + article.headline);
            System.out.println("Contents: " + article.contents);
            System.out.println("URL: " + article.url);
            System.out.println("-------------------");
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}