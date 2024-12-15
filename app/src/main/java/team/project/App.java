package team.project;

import team.project.datacollection.Crawler;

import java.util.ArrayList;
import java.util.List;

public class App {

    // 각 크롤러 작업을 스레드로 처리하기 위한 내부 클래스
    static class CrawlerTask implements Runnable {
        private final String threadName; // 스레드 이름
        private final Crawler crawlerInstance; // Crawler 인스턴스

        // CrawlerTask 생성자: 스레드 이름과 크롤러 인스턴스를 초기화
        public CrawlerTask(String threadName) {
            this.threadName = threadName;
            this.crawlerInstance = new Crawler();
        }

        // 스레드에서 실행될 작업 정의
        @Override
        public void run() {
            System.out.println("[" + threadName + "] 크롤링 시작...");
            try {
                crawlerInstance.crawl(); // 크롤링 작업 수행
                System.out.println("[" + threadName + "] 크롤링 완료.");
            } catch (Exception e) {
                System.err.println("[" + threadName + "] 오류 발생: " + e.getMessage());
            }
        }

        // 크롤링된 기사 리스트를 반환
        public List<String> getArticles() {
            return crawlerInstance.getArticlesString();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 애플리케이션 시작 ===");

        final int numberOfThreads = 3; // 사용할 스레드 수
        List<Thread> threads = new ArrayList<>(); // 스레드를 저장할 리스트
        List<CrawlerTask> tasks = new ArrayList<>(); // 작업(Task)을 저장할 리스트

        // 크롤러 스레드 생성 및 시작
        for (int i = 0; i < numberOfThreads; i++) {
            String threadName = "CrawlerThread-" + (i + 1); // 스레드 이름 지정
            CrawlerTask task = new CrawlerTask(threadName); // CrawlerTask 생성
            Thread thread = new Thread(task); // CrawlerTask를 실행할 스레드 생성

            threads.add(thread); // 스레드 리스트에 추가
            tasks.add(task); // 작업 리스트에 추가

            thread.start(); // 스레드 시작
        }

        // 모든 스레드 작업이 끝날 때까지 대기
        for (Thread thread : threads) {
            try {
                thread.join(); // 해당 스레드의 종료를 대기
            } catch (InterruptedException e) {
                System.err.println("스레드 대기 중 오류 발생: " + e.getMessage());
            }
        }

        // 모든 스레드에서 수집된 기사 결과를 통합
        System.out.println("=== 결과 통합 중 ===");
        List<String> allArticles = new ArrayList<>(); // 통합된 기사 리스트
        for (CrawlerTask task : tasks) {
            allArticles.addAll(task.getArticles()); // 각 작업(Task)의 기사 리스트 추가
        }

        // 통합된 기사 출력
        System.out.println("=== 수집된 기사 출력 ===");
        for (String article : allArticles) {
            System.out.println(article);
        }

        System.out.println("=== 애플리케이션 종료 ===");
    }
}