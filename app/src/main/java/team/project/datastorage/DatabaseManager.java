package team.project.datastorage;

import team.project.entity.Article;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://pro.freedb.tech:3306/stockAnalysis";
    private static final String USER = "testUser";
    private static final String PASSWORD = "GE?5#kmyUbD23zv";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
        initializeDatabase();
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection();
        PreparedStatement useStmt = conn.prepareStatement("USE stockAnalysis")){
            useStmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 단일 article 분석 결과 저장
    public static void storeData(Article article, String estimateValue) {
        String sql = "INSERT INTO article_evaluations (headline, contents, url, estimate_value) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, article.headline);
            pstmt.setString(2, article.contents);
            pstmt.setString(3, article.url);
            pstmt.setString(4, estimateValue);
            pstmt.executeUpdate();
            
            System.out.println("Article data stored successfully for URL: " + article.url);
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
                //System.out.println("Contents: " + rs.getString("contents")); // 기사 원문 출력
                System.out.println("URL: " + rs.getString("url"));
                System.out.println("Estimate Value: " + rs.getDouble("estimate_value"));
                System.out.println("Evaluation Date: " + rs.getTimestamp("evaluation_date"));
                System.out.println("-------------------------------------");
            }
            
        } catch (SQLException e) {
            System.err.println("Error printing all data: " + e.getMessage());
        }
    }

    // URL로 기사 검색 및 출력
    public static void searchData(String url) {
        String sql = "SELECT * FROM article_evaluations WHERE url = ?";
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, url);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n=== 기사 데이터 ===");
                System.out.println("-------------------");
                System.out.println("Headline: " + rs.getString("headline"));
                System.out.println("Content: " + rs.getString("contents"));
                System.out.println("URL: " + rs.getString("url"));
                System.out.println("Estimate value: " + rs.getString("estimate_value"));
                System.out.println("Evaluation Date: " + rs.getTimestamp("evaluation_date"));
                System.out.println("-------------------");
            } else {
                System.out.println("URL: " + url + "에 해당하는 데이터를 찾을 수 없습니다.");
            }
            
        } catch (SQLException e) {
            System.err.println("데이터 검색 중 오류 발생: " + e.getMessage());
        }
    }

    // 모든 데이터 삭제
    public static void deleteAllData() {
        String sql = "DELETE FROM article_evaluations";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rowCount = stmt.executeUpdate(sql);
            System.out.println("총 " + rowCount + "개의 데이터가 삭제되었습니다.");
            
        } catch (SQLException e) {
            System.err.println("데이터 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    // URL로 특정 데이터 삭제
    public static void deleteDataByUrl(String url) {
        String sql = "DELETE FROM article_evaluations WHERE url = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, url);
            int rowCount = pstmt.executeUpdate();
            
            if (rowCount > 0) {
                System.out.println("URL: " + url + "의 데이터가 삭제되었습니다.");
            } else {
                System.out.println("URL: " + url + "에 해당하는 데이터를 찾을 수 없습니다.");
            }
            
        } catch (SQLException e) {
            System.err.println("데이터 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}