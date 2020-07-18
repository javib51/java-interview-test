package se.kry.codetest.bss;

import io.vertx.core.Future;
import se.kry.codetest.DBConnector;
import se.kry.codetest.dao.ServiceDao;
import se.kry.codetest.models.ServiceEntity;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static io.vertx.core.Future.future;

public class ServicesController {
    ServiceDao dao;

    public ServicesController(DBConnector dbConnector) {
        this.dao = new ServiceDao(dbConnector);
    }

    public Future<ServiceEntity> deleteService(int id) {
        Future<ServiceEntity> resultFuture = future();
        // Check if it exists first
        // This is not a requirement itself but its a common practice
        dao.existsEntity(id).setHandler(done -> {
            if (done.failed()) {
                resultFuture.fail(done.cause());
            } else {
                dao.deleteEntity(id).setHandler(doneDelete -> {
                    if (doneDelete.failed()) {
                        resultFuture.fail(doneDelete.cause());
                    } else {
                        resultFuture.complete();
                    }
                });
            }
        });
        return resultFuture;
    }

    public Future<ServiceEntity> addService(String name, String serviceUrl) {
        // check if url is correct
        try {
            URL obj = new URL(serviceUrl);
            obj.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return Future.failedFuture("url is not valid");
        }

        // Create service
        ServiceEntity service = new ServiceEntity(name, serviceUrl, "UNKNOWN");
        return dao.addEntity(service);
    }

    public Future<ServiceEntity> updateServiceStatus(int id, String status) {
        return dao.updateEntityStatus(id, status);
    }

    public Future<List<ServiceEntity>> getServices() {
        return dao.getEntities();
    }
}
