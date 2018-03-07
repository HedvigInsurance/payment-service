package com.hedvig.paymentservice.web;

import com.google.gson.Gson;
import com.hedvig.paymentService.trustly.NotificationHandler;
import com.hedvig.paymentService.trustly.commons.ResponseStatus;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.response.Response;
import com.hedvig.paymentservice.services.trustly.TrustlyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hooks/trustly/")
public class TrustlyNotificationController {

    private final Logger log = LoggerFactory.getLogger(TrustlyNotificationController.class);
    private final TrustlyService trustlyService;

    public TrustlyNotificationController(TrustlyService trustlyService) {
        this.trustlyService = trustlyService;
    }

    @PostMapping(value = "notifications", produces = "application/json")
    public ResponseEntity<?> notifications(@RequestBody String requestBody) {

        NotificationHandler handler = new NotificationHandler();
        final Notification notification = handler.handleNotification(requestBody);

        log.info("Notification received from trustly: {}", requestBody);

        final ResponseStatus responseStatus = trustlyService.recieveNotification(notification);

        final Response response = handler.prepareNotificationResponse(notification.getMethod(), notification.getUUID(), responseStatus);

        final Gson gson = new Gson();
        return ResponseEntity.ok(gson.toJson(response));
    }

}
