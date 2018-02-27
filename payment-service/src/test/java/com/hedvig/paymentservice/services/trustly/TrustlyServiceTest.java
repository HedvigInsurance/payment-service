package com.hedvig.paymentservice.services.trustly;


import com.hedvig.paymentService.trustly.SignedAPI;
import com.hedvig.paymentService.trustly.commons.Method;
import com.hedvig.paymentService.trustly.commons.ResponseStatus;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyConnectionException;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.notification.NotificationData;
import com.hedvig.paymentService.trustly.data.notification.NotificationParameters;
import com.hedvig.paymentService.trustly.data.request.Request;
import com.hedvig.paymentService.trustly.data.request.requestdata.SelectAccountData;
import com.hedvig.paymentService.trustly.data.response.Response;
import com.hedvig.paymentService.trustly.data.response.Result;
import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderState;
import com.hedvig.paymentservice.domain.trustlyOrder.OrderType;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceivedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateOrderCommand;
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrder;
import com.hedvig.paymentservice.query.trustlyOrder.enteties.TrustlyOrderRepository;
import com.hedvig.paymentservice.services.exceptions.OrderNotFoundException;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static com.hedvig.paymentservice.trustly.testHelpers.TestData.TRIGGER_ID;
import static com.hedvig.paymentservice.trustly.testHelpers.TestData.createDirectDebitRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TrustlyServiceTest {

    public static final String TRUSTLY_IFRAME_URL = "https://trustly.com/dbadbkabd/";
    public static final String TRUSTLY_ORDERID = "2190971587";
    public static final String MEMBER_ID = "1337";
    public static final String EXCEPTION_MESSAGE = "Could not connect to trustly";
    public static final String SUCCESS_URL = "https://hedvig.com/success";
    public static final String FAIL_URL = "https://hedvig.com/failure&triggerId";
    public static final String NOTIFICATION_URL = "https://gateway.test.hedvig.com/notificationHook";
    @Mock
    SignedAPI signedAPI;

    @Mock
    CommandGateway gateway;

    @Mock
    UUIDGenerator uuidGenerator;

    @Mock
    private TrustlyOrderRepository orderRepository;

    @Mock
    Environment springEnvironment;

    TrustlyService testService;

    @Captor
    ArgumentCaptor<Request> requestCaptor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    public static final UUID REQUEST_ID = UUID.randomUUID();


    @Before
    public void setUp() {

        given(springEnvironment.acceptsProfiles("development")).willReturn(true);

        given(uuidGenerator.generateRandom()).willReturn(REQUEST_ID);

        testService = new TrustlyService(signedAPI, gateway, uuidGenerator, orderRepository, SUCCESS_URL, FAIL_URL, NOTIFICATION_URL, springEnvironment);
    }

    @Test
    public void firsttest() {

        given(uuidGenerator.generateRandom()).willReturn(REQUEST_ID);

        final Response trustlyResponse = createResponse(TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID);
        given(signedAPI.sendRequest(any())).willReturn(trustlyResponse);

        final DirectDebitResponse directDebitResponse =
                testService.requestDirectDebitAccount(createDirectDebitRequest());

        assertThat(directDebitResponse.getUrl()).isEqualTo(TRUSTLY_IFRAME_URL);

        InOrder inOrder = Mockito.inOrder(gateway);

        inOrder.verify(gateway).sendAndWait(isA(CreateOrderCommand.class));
        inOrder.verify(gateway).sendAndWait(new SelectAccountResponseReceivedCommand(REQUEST_ID, TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID));

    }

    @Test
    public void requestDirectDebitAccount_setsMessageId_to_requestId(){

        final Response trustlyResponse = createResponse(TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID);
        given(signedAPI.sendRequest(requestCaptor.capture())).willReturn(trustlyResponse);

        testService.requestDirectDebitAccount(createDirectDebitRequest());

        SelectAccountData requestData = (SelectAccountData) requestCaptor.getValue().getParams().getData();
        assertThat(requestData.getMessageID()).isEqualTo(withQuotes(REQUEST_ID.toString()));
        assertThat(requestData.getEndUserID()).isEqualTo(withQuotes(MEMBER_ID));

    }

    @Test
    public void requestDirectDebitAccount_setsSuccessUrlFailURL(){

        final Response trustlyResponse = createResponse(TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID);
        given(signedAPI.sendRequest(requestCaptor.capture())).willReturn(trustlyResponse);

        testService.requestDirectDebitAccount(createDirectDebitRequest());

        SelectAccountData requestData = (SelectAccountData) requestCaptor.getValue().getParams().getData();
        assertThat(requestData.getAttributes().get("SuccessURL")).isEqualTo(withQuotes(SUCCESS_URL + "&triggerId=" +TRIGGER_ID));
        assertThat(requestData.getAttributes().get("FailURL")).isEqualTo(withQuotes(FAIL_URL +  "&triggerId=" +TRIGGER_ID));
        assertThat(requestData.getEndUserID()).isEqualTo(withQuotes(MEMBER_ID));

    }

    @Test
    public void requestDirectDebitAccount_setsNotificationURL(){
        final Response trustlyResponse = createResponse(TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID);
        given(signedAPI.sendRequest(requestCaptor.capture())).willReturn(trustlyResponse);

        testService.requestDirectDebitAccount(createDirectDebitRequest());

        SelectAccountData requestData = (SelectAccountData) requestCaptor.getValue().getParams().getData();
        assertThat(requestData.getNotificationURL()).isEqualTo(withQuotes(NOTIFICATION_URL));
    }



    @Test
    public void requestDirectDebitAccount_apiThrowsException() {

        TrustlyConnectionException exception = new TrustlyConnectionException(EXCEPTION_MESSAGE);
        given(signedAPI.sendRequest(requestCaptor.capture())).willThrow(exception);

        thrown.expect(RuntimeException.class);
        testService.requestDirectDebitAccount(createDirectDebitRequest());

        verify(gateway, atLeastOnce()).sendAndWait(new SelectAccountRequestFailedCommand(REQUEST_ID, EXCEPTION_MESSAGE));

    }

    @Test
    public void orderInformation_throwsOrderNotFoundException() {

        given(orderRepository.findById(REQUEST_ID)).willReturn(Optional.empty());

        thrown.expect(OrderNotFoundException.class);
        testService.orderInformation(REQUEST_ID);

    }

    @Test
    public void orderInformation_returnsOrderInformation(){
        final TrustlyOrder trustlyOrder = createTrustlyOrder();

        given(orderRepository.findById(REQUEST_ID)).willReturn(Optional.of(trustlyOrder));
    }

    public TrustlyOrder createTrustlyOrder() {
        final TrustlyOrder trustlyOrder = new TrustlyOrder();
        trustlyOrder.setType(OrderType.SELECT_ACCOUNT);
        trustlyOrder.setState(OrderState.STARTED);
        trustlyOrder.setTrustlyOrderId(TRUSTLY_ORDERID);
        trustlyOrder.setId(REQUEST_ID);
        trustlyOrder.setIframeUrl(TRUSTLY_IFRAME_URL);
        return trustlyOrder;
    }


    @Test
    public void test_Notification() {

        Notification notification = new Notification();
        final NotificationParameters params = new NotificationParameters();
        notification.setParams(params);
        final NotificationData data = new NotificationData();
        params.setData(data);
        data.setNotificationId(withQuotes("0182309810381"));
        data.setMessageId(withQuotes(REQUEST_ID.toString()));
        final ResponseStatus responseStatus = testService.recieveNotification(notification);

        assertThat(responseStatus).isEqualTo(ResponseStatus.OK);
    }

    private String withQuotes(String requestId) {
        return String.format("%s",requestId);
    }

    private Response createResponse(String iframeUrl, String orderid) {
        final Response response = new Response();

        final Result result = new Result();
        result.setMethod(Method.SELECT_ACCOUNT);
        result.setUuid(UUID.randomUUID().toString());
        HashMap<String, Object> data = new HashMap<>();
        data.put("url", iframeUrl);
        data.put("orderid", orderid);

        result.setData(data);
        response.setResult(result);

        return response;
    }

}