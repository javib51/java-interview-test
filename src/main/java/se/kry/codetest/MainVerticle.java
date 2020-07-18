package se.kry.codetest;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.bss.ServicesController;
import se.kry.codetest.models.ServiceEntity;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

    private DBConnector connector;
    private ServicesController controller;
    private BackgroundPoller poller;

    /**
     * No a fan of this function, many independent setups are done together and mixed. With NO COMMENTS
     * I am not an vertx expert, so I will leave it like it is. I just resorted a bit to make it clear for me.
     */
    @Override
    public void start(Future<Void> startFuture) {
        // Set Connector
        connector = new DBConnector(vertx);

        // Set Services controller
        controller = new ServicesController(connector);

        // Set Router and routes
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        setRoutes(router);

        // Cron for services
        poller = new BackgroundPoller(vertx, controller);
        vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices());

        // Start Http server
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());
        router.get("/service").handler(this::getServices);
        router.post("/service").handler(this::newService);
        router.delete("/service/:id").handler(this::deleteService);
    }

    /**
     * This method normally goes in another file.
     * I just split it for readability purposes
     *
     * @param req
     */
    private void newService(RoutingContext req) {
        JsonObject jsonBody = req.getBodyAsJson();
        controller.addService(jsonBody.getString("name"), jsonBody.getString("url")).setHandler(opDone -> {
            if (opDone.failed()) {
                req.response()
                        .setStatusCode(HttpResponseStatus.FAILED_DEPENDENCY.code()).end();
            } else {
                req.response()
                        .putHeader("content-type", "text/plain")
                        .end("OK");
            }
        });
    }

    /**
     * This method normally goes in another file.
     * I just split it for readability purposes
     *
     * @param req
     */
    private void getServices(RoutingContext req) {
        controller.getServices().setHandler(done -> {
            if (done.failed()) {
                req.response()
                        .setStatusCode(HttpResponseStatus.FAILED_DEPENDENCY.code()).end();
            } else {
                List<JsonObject> jsonServices = done.result()
                        .stream()
                        .map(service ->
                                new JsonObject()
                                        .put("name", service.getName())
                                        .put("status", service.getStatus())
                                        .put("url", service.getUrl())
                                        .put("created_at", service.getCreationDateISO8601())
                                        .put("id", service.getId())
                        )
                        .collect(Collectors.toList());

                req.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonArray(jsonServices).encode());
            }
        });
    }

    /**
     * This method normally goes in another file.
     * I just split it for readability purposes
     *
     * @param request
     */
    private void deleteService(RoutingContext request) {
        final String idParam = request.request().getParam("id");
        final int id = Integer.parseInt(idParam);

        controller.deleteService(id).setHandler(opDone -> {
            if (opDone.failed()) {
                request.response()
                        .setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
            } else {
                request.response()
                        .putHeader("content-type", "text/plain")
                        .end("OK");
            }
        });
    }

}


