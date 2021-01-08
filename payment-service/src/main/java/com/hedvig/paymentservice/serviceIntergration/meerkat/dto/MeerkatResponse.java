package com.hedvig.paymentservice.serviceIntergration.meerkat.dto;

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;

public final class MeerkatResponse {

  private final String query;
  private final SanctionStatus result;

    @java.beans.ConstructorProperties({"query", "result"})
    public MeerkatResponse(String query, SanctionStatus result) {
        this.query = query;
        this.result = result;
    }

    public String getQuery() {
        return this.query;
    }

    public SanctionStatus getResult() {
        return this.result;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MeerkatResponse)) return false;
        final MeerkatResponse other = (MeerkatResponse) o;
        final Object this$query = this.getQuery();
        final Object other$query = other.getQuery();
        if (this$query == null ? other$query != null : !this$query.equals(other$query)) return false;
        final Object this$result = this.getResult();
        final Object other$result = other.getResult();
        if (this$result == null ? other$result != null : !this$result.equals(other$result)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $query = this.getQuery();
        result = result * PRIME + ($query == null ? 43 : $query.hashCode());
        final Object $result = this.getResult();
        result = result * PRIME + ($result == null ? 43 : $result.hashCode());
        return result;
    }

    public String toString() {
        return "MeerkatResponse(query=" + this.getQuery() + ", result=" + this.getResult() + ")";
    }
}
