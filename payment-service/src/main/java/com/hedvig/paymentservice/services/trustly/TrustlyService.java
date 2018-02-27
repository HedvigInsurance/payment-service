package com.hedvig.paymentservice.services.trustly;


import com.google.gson.Gson;
import com.hedvig.paymentService.trustly.commons.ResponseStatus;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyAPIException;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.response.Error;
import com.hedvig.paymentService.trustly.requestbuilders.SelectAccount;
import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.NotificationReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrder;
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrderRepository;
import com.hedvig.paymentservice.services.exceptions.OrderNotFoundException;
import com.hedvig.paymentservice.services.trustly.dto.DirectDebitRequest;
import com.hedvig.paymentservice.services.trustly.dto.OrderInformation;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import com.hedvig.paymentService.trustly.SignedAPI;
import com.hedvig.paymentService.trustly.data.request.Request;
import com.hedvig.paymentService.trustly.data.response.Response;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class TrustlyService {

    private final String notificationUrl;
    public static final String COUNTRY = "SE";
    private final Logger log = LoggerFactory.getLogger(TrustlyService.class);
    private final SignedAPI api;
    private final CommandGateway gateway;
    private final UUIDGenerator uuidGenerator;
    private final String successUrl;
    private final String failUrl;

    private final TrustlyOrderRepository orderRepository;

    private final Environment springEnvironmnet;

    public TrustlyService(SignedAPI api, CommandGateway gateway, UUIDGenerator uuidGenerator, TrustlyOrderRepository orderRepository, @Value("${hedvig.trustly.successURL}") String successUrl, @Value("${hedvig.trustly.failURL}") String failUrl, @Value("${hedvig.trustly.notificationURL}") String notificationUrl, Environment springEnvironmnet) {
        this.api = api;
        this.gateway = gateway;
        this.uuidGenerator = uuidGenerator;
        this.orderRepository = orderRepository;
        this.successUrl = successUrl;
        this.failUrl = failUrl;
        this.notificationUrl = notificationUrl;
        this.springEnvironmnet = springEnvironmnet;
    }

    public DirectDebitResponse requestDirectDebitAccount(DirectDebitRequest request) {

        final UUID requestId = uuidGenerator.generateRandom();

        gateway.sendAndWait(new CreateOrderCommand(request.getMemberId(), requestId));

        return startTrustlyOrder(request.getMemberId(), request, requestId);

    }

    public DirectDebitResponse startTrustlyOrder(String memberId, DirectDebitRequest request, UUID requestId) {
        try {
            final Request trustlyRequest = createRequest(requestId, memberId, request);
            final Response response = api.sendRequest(trustlyRequest);

            if (response.successfulResult()) {
                final Map<String, Object> data;
                data = response.getResult().getData();
                log.info("SelectAccount Order created at trustly with trustlyOrderId: {}, hedvigOrderId: {}", data.get("orderid"), requestId);


                gateway.sendAndWait(
                        new SelectAccountResponseReceivedCommand(requestId, (String) data.get("url"), (String) data.get("orderid")));

                return new DirectDebitResponse((String) data.get("url"), requestId.toString());
            } else {
                final Error error = response.getError();
                log.info("Order creation failed: {}, {}, {}", error.getName(), error.getCode(), error.getMessage());
                throw new RuntimeException("Got error from trustly");
            }
        }
        catch (TrustlyAPIException ex) {
            //gateway.sendAndWait(new SelectAccountRequestFailedCommand(requestId, ex.getMessage()));
            throw new RuntimeException("Failed calling trustly.", ex);
        }
    }

    private Request createRequest(UUID hedvigOrderId, String memberId, DirectDebitRequest request) {
        final SelectAccount.Build build = new SelectAccount.Build(notificationUrl, memberId, hedvigOrderId.toString());
        build.requestDirectDebitMandate("1");
        build.firstName(request.getFirstName());
        build.lastName(request.getLastName());
        build.country(COUNTRY);
        build.email(request.getEmail());
        build.locale("sv_SE");
        build.nationalIdentificationNumber(request.getSsn());
        build.successURL(appendTriggerId(successUrl, request.getTriggerId()));
        build.failURL(appendTriggerId(failUrl, request.getTriggerId()));



        final Request request1 = build.getRequest();
        final Gson gson = new Gson();
        log.info("Trustly request details: {}", gson.toJson(request1));

        if(springEnvironmnet.acceptsProfiles("development")) {
            request1.getParams().getData().getAttributes().put("HoldNotifications", "1");
        }

        return request1;
    }

    private String appendTriggerId(String failUrl, String triggerId) {
        return failUrl + "&triggerId=" + triggerId;
    }

    public Response sendRequest(Request request) {
        return api.sendRequest(request);
    }

    public ResponseStatus recieveNotification(Notification notification) {

        log.info("Received notification from Trustly: {}", notification.getMethod());

        UUID requestId = UUID.fromString(notification.getParams().getData().getMessageId());

        gateway.sendAndWait(new NotificationReceivedCommand(requestId, notification));

        return ResponseStatus.OK;
    }

    public OrderInformation orderInformation(UUID requestId) {

        final Optional<TrustlyOrder> byId = this.orderRepository.findById(requestId);
        final TrustlyOrder trustlyOrder = byId.orElseThrow(() -> new OrderNotFoundException("Order not found with id " + requestId));

        return new OrderInformation(requestId, trustlyOrder.getIframeUrl(), trustlyOrder.getState());
    }
}
