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

package com.hedvig.paymentService.trustly.requestbuilders;

import com.hedvig.paymentService.trustly.commons.Currency;
import com.hedvig.paymentService.trustly.commons.Method;
import com.hedvig.paymentService.trustly.data.request.Request;
import com.hedvig.paymentService.trustly.data.request.RequestParameters;
import com.hedvig.paymentService.trustly.data.request.requestdata.ViewAutomaticSettlementDetailsCSVData;
import com.hedvig.paymentService.trustly.security.SignatureHandler;
import java.util.Map;

/**
 * Creates a ViewAutomaticSettlementDetailsCSV request ready to be sent to Trustly API. The
 * constructor contains the required fields of a ViewAutomaticSettlementDetailsCSV request.
 *
 * <p>Builder lets you add additional information if any is available for the given request.
 *
 * <p>The API specifics of the request can be found on https://trustly.com/en/developer/
 *
 * <p>Example use for a default ViewAutomaticSettlementDetailsCSV request: Request
 * viewAutomaticSettlementDetailsCsv = new ViewAutomaticSettlementDetailsCSV.Build(settlementDate,
 * currency).getRequest();
 */
public class ViewAutomaticSettlementDetailsCSV {
  private final Request request = new Request();

  private ViewAutomaticSettlementDetailsCSV(final Build builder) {
    final RequestParameters params = new RequestParameters();
    params.setUUID(SignatureHandler.generateNewUUID());
    params.setData(builder.data);

    request.setMethod(Method.VIEW_AUTOMATIC_SETTLEMENT_DETAILS_CSV);
    request.setParams(params);
  }

  public Request getRequest() {
    return request;
  }

  public static class Build {
    private final ViewAutomaticSettlementDetailsCSVData data =
        new ViewAutomaticSettlementDetailsCSVData();
    // Kept for future use
    private final Map<String, Object> attributes = null;

    public Build(final String settlementDate, final Currency currency) {
      data.setSettlementDate(settlementDate);
      data.setCurrency(currency);
      data.setAttributes(attributes);
    }

    public Request getRequest() {
      return new ViewAutomaticSettlementDetailsCSV(this).getRequest();
    }
  }
}
