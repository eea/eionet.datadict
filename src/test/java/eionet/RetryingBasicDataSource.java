package eionet;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;

public class RetryingBasicDataSource extends BasicDataSource {

    public void initializeWithRetry() throws InterruptedException {
        System.out.println("Connecting to DB with URL: " + this.getUrl() + " -- " + this.getUsername() + "-" + this.getPassword()); // log the URL
        int retries = 5;
        int waitMs = 5000;

        for (int i = 1; i <= retries; i++) {
            try (Connection conn = this.getConnection()) {
                System.out.println("DB connection success on attempt " + i);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("DB connection failed on attempt " + i + ", retrying in " + waitMs + "ms");
                if (i == retries) {
                    throw new RuntimeException("Could not connect to DB after " + retries + " attempts", e);
                }
                Thread.sleep(waitMs);
            }
        }
    }
}
