package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DBConnector connector = new DBConnector(vertx);

        final String migrationQuery = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS service (")
                .append("id INTEGER PRIMARY KEY,")
                .append("name VARCHAR(20) NOT NULL,")
                .append("status VARCHAR(10) NOT NULL,")
                .append("url VARCHAR(128) NOT NULL,")
                .append("created_at DATETIME NOT NULL")
                .append(")")
                .toString();
        connector.query(migrationQuery).
                setHandler(done -> {
                    if (done.succeeded()) {
                        System.out.println("completed db migrations");
                    } else {
                        done.cause().printStackTrace();
                    }
                    vertx.close(shutdown -> {
                        System.exit(0);
                    });
                });
    }
}
