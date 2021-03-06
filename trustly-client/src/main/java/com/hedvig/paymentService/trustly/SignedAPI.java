/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Trustly Group AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hedvig.paymentService.trustly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyConnectionException;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlyDataException;
import com.hedvig.paymentService.trustly.commons.exceptions.TrustlySignatureException;
import com.hedvig.paymentService.trustly.data.request.Request;
import com.hedvig.paymentService.trustly.data.response.Response;
import com.hedvig.paymentService.trustly.security.SignatureHandler;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.security.SecureRandom;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SignedAPI {

  private final SignatureHandler signatureHandler = SignatureHandler.getInstance();

  private static final String TEST_ENVIRONMENT_API_URL = "https://test.trustly.com/api/1";
  private static final String LIVE_ENVIRONMENT_API_URL = "https://trustly.com/api/1";
  private static String apiUrl;

  /**
   * Method used for initializing a SignatureHandler.
   *
   * @param keyPassword Seeing that the private key is somewhat considered a password in itself, the
   *                    private key password is usually an empty string.
   */
  public void init(
    final String privateKeyPath,
    final String keyPassword,
    final String usernamePremium,
    final String passwordPremium,
    final String usernameClaimHdi,
    final String passwordClaimHdi,
    final String usernameClaimHedvig,
    final String passwordClaimHedvig
  )
    throws URISyntaxException {
    init(
      privateKeyPath,
      keyPassword,
      usernamePremium,
      passwordPremium,
      usernameClaimHdi,
      passwordClaimHdi,
      usernameClaimHedvig,
      passwordClaimHedvig,
      false
    );
  }

  /**
   * Method used for initializing a SignatureHandler.
   *
   * @param keyPassword Seeing that the private key is somewhat considered a password in itself, the
   *                    private key password is usually an empty string.
   */
  public void init(
    final String privateKeyPath,
    final String keyPassword,
    final String usernamePremium,
    final String passwordPremium,
    final String usernameClaimHdi,
    final String passwordClaimHdi,
    final String usernameClaimHedvig,
    final String passwordClaimHedvig,
    final boolean testEnvironment
  )
    throws URISyntaxException {
    setEnvironment(testEnvironment);
    try {
      signatureHandler.init(privateKeyPath,
        keyPassword,
        usernamePremium,
        passwordPremium,
        usernameClaimHdi,
        passwordClaimHdi,
        usernameClaimHedvig,
        passwordClaimHedvig,
        testEnvironment);
    } catch (final KeyException e) {
      e.printStackTrace();
    }
  }

  private void setEnvironment(final boolean testEnvironment) {
    apiUrl = testEnvironment ? TEST_ENVIRONMENT_API_URL : LIVE_ENVIRONMENT_API_URL;
  }

  /**
   * Sends given request to Trustly.
   *
   * @param request Request to send to Trustly API
   * @return Response generated from the request.
   */
  public Response sendRequest(final Request request, final Account account) {
    final Gson gson = new GsonBuilder().serializeNulls().create();

    if (account == Account.CLAIM_HEDVIG) {
      signatureHandler.insertClaimHedvigAccountCredentials(request);
    } else if (account == Account.CLAIM_HDI) {
      signatureHandler.insertClaimHdiAccountCredentials(request);
    } else {
      signatureHandler.insertCredentials(request);
    }

    signatureHandler.signRequest(request);

    final String jsonResponse = newHttpPost(gson.toJson(request, Request.class));

    return handleJsonResponse(jsonResponse, request.getUUID());
  }

  /**
   * Sends a POST data to Trustly server.
   *
   * @param request String representation of a request.
   * @return String representation of a response.
   */
  private String newHttpPost(final String request) {
    try {
      final CloseableHttpClient httpClient = HttpClients.createDefault();
      final HttpPost httpPost = new HttpPost(apiUrl);
      final StringEntity jsonRequest = new StringEntity(request, "UTF-8");
      httpPost.addHeader("content-type", "application/json");
      httpPost.setEntity(jsonRequest);

      final HttpResponse result = httpClient.execute(httpPost);
      return EntityUtils.toString(result.getEntity(), "UTF-8");
    } catch (final IOException e) {
      throw new TrustlyConnectionException("Failed to send request.", e);
    }
  }

  /**
   * Deserializes and verifies incoming response.
   *
   * @param responseJson response from Trustly.
   * @param requestUUID  UUID from the request that resulted in the response.
   * @return Response object
   */
  private Response handleJsonResponse(final String responseJson, final String requestUUID) {
    final Gson gson = new Gson();
    final Response response = gson.fromJson(responseJson, Response.class);
    verifyResponse(response, requestUUID);
    return response;
  }

  private void verifyResponse(final Response response, final String requestUUID) {
    if (!signatureHandler.verifyResponseSignature(response)) {
      throw new TrustlySignatureException("Incoming data signature is not valid");
    }
    if (response.getUUID() != null && !response.getUUID().equals(requestUUID)) {
      throw new TrustlyDataException("Incoming data signature is not valid");
    }
  }

  /**
   * Generates a random messageID. Good for testing.
   *
   * @return return a random generated messageid.
   */
  public String newMessageID() {
    final SecureRandom random = new SecureRandom();

    return new BigInteger(130, random).toString(32);
  }
}
