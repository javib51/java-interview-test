package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
//import io.vertx.rxjava.circuitbreaker.CircuitBreaker;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import se.kry.codetest.bss.ServicesController;
import se.kry.codetest.models.ServiceEntity;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class BackgroundPoller {
    private final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    //  private final CircuitBreaker breaker;
    private ServicesController controller;
    private Vertx vertx;

    public BackgroundPoller(Vertx vertx, ServicesController controller) {
//     breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
//            new CircuitBreakerOptions()
//                    .setMaxFailures(5) // number of failure before opening the circuit
//                    .setTimeout(2000) // consider a failure if the operation does not succeed in time
//                    .setFallbackOnFailure(true) // do we call the fallback on failure
//                    .setResetTimeout(10000) // time spent in open state before attempting to re-try
//    );
        this.vertx = vertx;
        this.controller = controller;
    }

    /**
     * I decided to make the http calls async with a timeout. So, each of them execute independenly with 2s timeout.
     * They directly save the info into the db
     * @return
     */
    public Future pollServices() {
        Future future = Future.future();
        // use logger.info instead of system println ??
        System.out.println("Check Services health starting...");
        controller.getServices().setHandler(done -> {
            if (done.failed()) {
                System.err.println("Fails getting services from db");
                future.fail("Fails getting services from db");
            } else {
                done.result().stream().forEach(service -> {
                    healthCheck(service);
                });
                future.complete();
            }
        });
        return future;
    }

    private void healthCheck(ServiceEntity service) {
        WebClientOptions clientOptions = new WebClientOptions().setConnectTimeout(2000);
        WebClient client = WebClient.create(vertx, clientOptions);

        client.getAbs(service.getUrl()).send(done -> {
            if (done.succeeded()) {
                controller.updateServiceStatus(service.getId(), "OK").setHandler(statusHandler -> {
                    if (done.failed()) {
                        System.err.println("service:" + service.getId() + " Updating status fails");
                    } else {
                        System.out.println("service:" + service.getId() + " updated correctly");
                    }
                });
            } else {
                controller.updateServiceStatus(service.getId(), "FAIL").setHandler(statusHandler -> {
                    if (done.failed()) {
                        System.err.println("service:" + service.getId() + " Updating status fails");
                    } else {
                        System.out.println("service:" + service.getId() + " updated correctly");
                    }
                });
            }
        });
    }
}
