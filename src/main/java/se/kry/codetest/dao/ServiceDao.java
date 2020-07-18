package se.kry.codetest.dao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import se.kry.codetest.DBConnector;
import se.kry.codetest.models.ServiceEntity;

import javax.swing.text.html.parser.Entity;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.vertx.core.Future.future;

public class ServiceDao {
    private DBConnector dbConnector;

    public ServiceDao(DBConnector dbConnector) {
        this.dbConnector = dbConnector;
    }

    public Future<ServiceEntity> addEntity(ServiceEntity service){
        Future<ServiceEntity> resultFuture = Future.future();

        // Prepare params for sql stmt

        final JsonArray queryParams = new JsonArray()
                .add(service.getName())
                .add(service.getUrl())
                .add(service.getStatus())
                .add(service.getCreationDateISO8601());

        // Execute sql insert
        dbConnector.query("INSERT INTO service (name, url, status, created_at) VALUES (?, ?, ?, ?)", queryParams).setHandler(done -> {
            if (done.failed()) {
                resultFuture.fail(done.cause());
            } else {
                resultFuture.complete();
            }
        });

        return resultFuture;
    }

    public Future<List<ServiceEntity>> getEntities() {
        Future<List<ServiceEntity>> resultFuture = future();

        // Execute sql select
        dbConnector.query("SELECT id,name, url, status, created_at FROM service").setHandler(done -> {
            if (done.failed()) {
                resultFuture.fail(done.cause());
            } else {
                List results = fromResultSetToServiceList(done.result());
                resultFuture.complete(results);
            }
        });

        return resultFuture;
    }

    public Future<ServiceEntity> existsEntity(int id) {
        Future<ServiceEntity> resultFuture = future();

        final JsonArray queryParams = new JsonArray().add(id);
        // Execute sql select
        dbConnector.query("SELECT id FROM service WHERE id = ?", queryParams).setHandler(done -> {
            if (done.failed() || done.result().getRows().size() == 0 ) {
                resultFuture.fail(done.cause());
            } else {
                resultFuture.complete();
            }
        });

        return resultFuture;
    }

    public Future<ServiceEntity> deleteEntity(int id) {
        Future<ServiceEntity> resultFuture = future();

        final JsonArray queryParams = new JsonArray().add(id);
        // Execute sql select
        dbConnector.query("DELETE FROM service WHERE id = ?", queryParams).setHandler(done -> {
            if (done.failed()) {
                resultFuture.fail(done.cause());
            } else {
                resultFuture.complete();
            }
        });

        return resultFuture;
    }

    public Future<ServiceEntity> updateEntityStatus(int id, String status) {
        Future<ServiceEntity> resultFuture = future();

        final JsonArray queryParams = new JsonArray().add(status).add(id);
        // Execute sql select
        dbConnector.query("UPDATE service SET status = ? WHERE id = ?", queryParams).setHandler(done -> {
            if (done.failed()) {
                resultFuture.fail(done.cause());
            } else {
                resultFuture.complete();
            }
        });

        return resultFuture;
    }

    private List<ServiceEntity> fromResultSetToServiceList(ResultSet result) {
        List<ServiceEntity> res = new ArrayList();

        for (JsonObject el : result.getRows()) {
            final int id = el.getInteger("id");
            final String name = el.getString("name");
            final String url = el.getString("url");
            final String status = el.getString("status");

            Date createdDate = null;
            try {
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                createdDate = df.parse(el.getString("created_at"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            res.add(new ServiceEntity(id, name, url, status, createdDate));
        }

        return res;
    }

}
