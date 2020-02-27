package com.adobe.target.edge.client.local;

import com.adobe.target.edge.client.ClientConfig;
import com.adobe.target.edge.client.model.TargetDeliveryRequest;
import com.adobe.target.edge.client.service.TargetClientException;
import com.adobe.target.edge.client.service.TargetExceptionHandler;
import com.adobe.target.edge.client.service.TargetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class NotificationDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final TargetService targetService;
    private ThreadPoolExecutor executor;

    public NotificationDeliveryService(TargetService targetService) {
        this.targetService = targetService;
    }

    public void sendNotification(final TargetDeliveryRequest deliveryRequest) {
        this.executor.execute(() ->
                NotificationDeliveryService.this.targetService.executeNotification(deliveryRequest));
    }

    public void start(ClientConfig clientConfig) {
        this.executor = new ThreadPoolExecutor(2,
                50,
                1,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(500),
                (r, executor) -> {
                    RejectedExecutionException e = new RejectedExecutionException("Local-decisioning notification queue full");
                    TargetExceptionHandler handler = clientConfig.getExceptionHandler();
                    if (handler != null) {
                        handler.handleException(new TargetClientException("Local-decisioning notification rejected", e));
                    }
                    throw e;
                });
    }

    public void stop() {
        try {
            this.executor.awaitTermination(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            logger.info("Caught InterruptedException while shutting down NotificationDeliveryService: " + e);
        }
    }

}
