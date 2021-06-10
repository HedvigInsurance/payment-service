package com.hedvig.paymentservice.web.dtos;

import com.hedvig.paymentservice.graphQl.types.PayinMethodStatus;

public final class DirectDebitStatusDTO {
  private final String memberId;
  private final Boolean directDebitActivated;
  private final PayinMethodStatus directDebitStatus;

    @java.beans.ConstructorProperties({"memberId", "directDebitActivated"})
    public DirectDebitStatusDTO(
        String memberId,
        Boolean directDebitActivated,
        PayinMethodStatus directDebitStatus
    ) {
        this.memberId = memberId;
        this.directDebitActivated = directDebitActivated;
        this.directDebitStatus = directDebitStatus;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public Boolean getDirectDebitActivated() {
        return this.directDebitActivated;
    }

    public PayinMethodStatus getDirectDebitStatus() { return directDebitStatus; }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DirectDebitStatusDTO)) return false;
        final DirectDebitStatusDTO other = (DirectDebitStatusDTO) o;
        final Object this$memberId = this.getMemberId();
        final Object other$memberId = other.getMemberId();
        if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId))
            return false;
        final Object this$directDebitActivated = this.getDirectDebitActivated();
        final Object other$directDebitActivated = other.getDirectDebitActivated();
        if (this$directDebitActivated == null ? other$directDebitActivated != null : !this$directDebitActivated.equals(other$directDebitActivated))
            return false;
        final Object this$directDebitStatus = this.getDirectDebitStatus();
        final Object other$directDebitStatus = other.getDirectDebitStatus();
        if (this$directDebitStatus == null ? other$directDebitStatus != null : !this$directDebitStatus.equals(other$directDebitStatus))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $memberId = this.getMemberId();
        result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
        final Object $directDebitActivated = this.getDirectDebitActivated();
        result = result * PRIME + ($directDebitActivated == null ? 43 : $directDebitActivated.hashCode());
        final Object $directDebitStatus = this.getDirectDebitStatus();
        result = result * PRIME + ($directDebitStatus == null ? 43 : $directDebitStatus.hashCode());
        return result;
    }

    public String toString() {
        return "DirectDebitStatusDTO(memberId=" + this.getMemberId() + ", directDebitActivated=" + this.getDirectDebitActivated() +  ", directDebitStatus=" + this.getDirectDebitStatus() + ")";
    }
}
