package com.hedvig.paymentservice.graphQl.types;

public final class DirectDebitResponse {
  private final String url;
  private final String orderId;

    @java.beans.ConstructorProperties({"url", "orderId"})
    public DirectDebitResponse(String url, String orderId) {
        this.url = url;
        this.orderId = orderId;
    }

    public static DirectDebitResponse fromDirectDebitResposne(com.hedvig.paymentservice.web.dtos.DirectDebitResponse response) {
    return new DirectDebitResponse(response.getUrl(), response.getOrderId());
  }

    public String getUrl() {
        return this.url;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DirectDebitResponse)) return false;
        final DirectDebitResponse other = (DirectDebitResponse) o;
        final Object this$url = this.getUrl();
        final Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
        final Object this$orderId = this.getOrderId();
        final Object other$orderId = other.getOrderId();
        if (this$orderId == null ? other$orderId != null : !this$orderId.equals(other$orderId)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        final Object $orderId = this.getOrderId();
        result = result * PRIME + ($orderId == null ? 43 : $orderId.hashCode());
        return result;
    }

    public String toString() {
        return "DirectDebitResponse(url=" + this.getUrl() + ", orderId=" + this.getOrderId() + ")";
    }
}
