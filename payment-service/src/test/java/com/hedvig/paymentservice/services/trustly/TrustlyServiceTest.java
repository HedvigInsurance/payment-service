package com.hedvig.paymentservice.services.trustly;


import com.hedvig.paymentService.trustly.SignedAPI;
import com.hedvig.paymentService.trustly.commons.Method;
import com.hedvig.paymentService.trustly.commons.ResponseStatus;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyConnectionException;
import com.hedvig.paymentService.trustly.data.notification.Notification;
import com.hedvig.paymentService.trustly.data.request.Request;
import com.hedvig.paymentService.trustly.data.request.requestdata.SelectAccountData;
import com.hedvig.paymentService.trustly.data.response.Response;
import com.hedvig.paymentService.trustly.data.response.Result;
import com.hedvig.paymentservice.common.UUIDGenerator;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.SelectAccountResponseReceviedCommand;
import com.hedvig.paymentservice.domain.trustlyOrder.commands.CreateTrustlySelectAccountOrderCommand;
import com.hedvig.paymentservice.web.dtos.DirectDebitResponse;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.UUID;

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
    public static final String FAIL_URL = "https://hedvig.com/failure";
    public static final String NOTIFICATION_URL = "https://gateway.test.hedvig.com/notificationHook";
    @Mock
    SignedAPI signedAPI;

    @Mock
    CommandGateway gateway;

    @Mock
    UUIDGenerator uuidGenerator;

    TrustlyService testService;

    @Captor
    ArgumentCaptor<Request> requestCaptor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    public static final UUID REQUEST_ID = UUID.randomUUID();

    @Before
    public void setUp() {
        given(uuidGenerator.generateRandom()).willReturn(REQUEST_ID);

        testService = new TrustlyService(signedAPI, gateway, uuidGenerator, SUCCESS_URL, FAIL_URL, NOTIFICATION_URL);
    }

    @Test
    public void firsttest() {

        given(uuidGenerator.generateRandom()).willReturn(REQUEST_ID);

        final Response trustlyResponse = createResponse(TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID);
        given(signedAPI.sendRequest(any())).willReturn(trustlyResponse);

        final DirectDebitResponse directDebitResponse =
                testService.requestDirectDebitAccount("1337", createDirectDebitRequest());

        assertThat(directDebitResponse.getUrl()).isEqualTo(TRUSTLY_IFRAME_URL);

        InOrder inOrder = Mockito.inOrder(gateway);

        inOrder.verify(gateway).sendAndWait(isA(CreateTrustlySelectAccountOrderCommand.class));
        inOrder.verify(gateway).sendAndWait(new SelectAccountResponseReceviedCommand(REQUEST_ID, TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID));

    }

    @Test
    public void requestDirectDebitAccount_setsMessageId_to_requestId(){

        final Response trustlyResponse = createResponse(TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID);
        given(signedAPI.sendRequest(requestCaptor.capture())).willReturn(trustlyResponse);

        testService.requestDirectDebitAccount(MEMBER_ID, createDirectDebitRequest());

        SelectAccountData requestData = (SelectAccountData) requestCaptor.getValue().getParams().getData();
        assertThat(requestData.getMessageID()).isEqualTo(withQuotes(REQUEST_ID.toString()));
        assertThat(requestData.getEndUserID()).isEqualTo(withQuotes(MEMBER_ID));

    }

    @Test
    public void requestDirectDebitAccount_setsSuccessUrlFailURL(){

        final Response trustlyResponse = createResponse(TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID);
        given(signedAPI.sendRequest(requestCaptor.capture())).willReturn(trustlyResponse);

        testService.requestDirectDebitAccount(MEMBER_ID, createDirectDebitRequest());

        SelectAccountData requestData = (SelectAccountData) requestCaptor.getValue().getParams().getData();
        assertThat(requestData.getAttributes().get("SuccessURL")).isEqualTo(withQuotes(SUCCESS_URL));
        assertThat(requestData.getAttributes().get("FailURL")).isEqualTo(withQuotes(FAIL_URL));
        assertThat(requestData.getEndUserID()).isEqualTo(withQuotes(MEMBER_ID));

    }

    @Test
    public void requestDirectDebitAccount_setsNotificationURL(){
        final Response trustlyResponse = createResponse(TRUSTLY_IFRAME_URL, TRUSTLY_ORDERID);
        given(signedAPI.sendRequest(requestCaptor.capture())).willReturn(trustlyResponse);

        testService.requestDirectDebitAccount(MEMBER_ID, createDirectDebitRequest());

        SelectAccountData requestData = (SelectAccountData) requestCaptor.getValue().getParams().getData();
        assertThat(requestData.getNotificationURL()).isEqualTo(withQuotes(NOTIFICATION_URL));
    }



    @Test
    public void requestDirectDebitAccount_apiThrowsException() {

        TrustlyConnectionException exception = new TrustlyConnectionException(EXCEPTION_MESSAGE);
        given(signedAPI.sendRequest(requestCaptor.capture())).willThrow(exception);

        thrown.expect(RuntimeException.class);
        testService.requestDirectDebitAccount(MEMBER_ID, createDirectDebitRequest());

        verify(gateway, atLeastOnce()).sendAndWait(new SelectAccountRequestFailedCommand(REQUEST_ID, EXCEPTION_MESSAGE));

    }


    @Test
    public void test_Notification() {

        Notification notification = new Notification();
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