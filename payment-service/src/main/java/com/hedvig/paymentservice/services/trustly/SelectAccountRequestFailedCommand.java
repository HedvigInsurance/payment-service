package com.hedvig.paymentservice.services.trustly;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

public final class SelectAccountRequestFailedCommand {
  @TargetAggregateIdentifier
  private final UUID requestId;

  private final String exceptionMessage;

    @java.beans.ConstructorProperties({"requestId", "exceptionMessage"})
    public SelectAccountRequestFailedCommand(UUID requestId, String exceptionMessage) {
        this.requestId = requestId;
        this.exceptionMessage = exceptionMessage;
    }

    public UUID getRequestId() {
        return this.requestId;
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SelectAccountRequestFailedCommand)) return false;
        final SelectAccountRequestFailedCommand other = (SelectAccountRequestFailedCommand) o;
        final Object this$requestId = this.getRequestId();
        final Object other$requestId = other.getRequestId();
        if (this$requestId == null ? other$requestId != null : !this$requestId.equals(other$requestId)) return false;
        final Object this$exceptionMessage = this.getExceptionMessage();
        final Object other$exceptionMessage = other.getExceptionMessage();
        if (this$exceptionMessage == null ? other$exceptionMessage != null : !this$exceptionMessage.equals(other$exceptionMessage))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $requestId = this.getRequestId();
        result = result * PRIME + ($requestId == null ? 43 : $requestId.hashCode());
        final Object $exceptionMessage = this.getExceptionMessage();
        result = result * PRIME + ($exceptionMessage == null ? 43 : $exceptionMessage.hashCode());
        return result;
    }

    public String toString() {
        return "SelectAccountRequestFailedCommand(requestId=" + this.getRequestId() + ", exceptionMessage=" + this.getExceptionMessage() + ")";
    }
}
