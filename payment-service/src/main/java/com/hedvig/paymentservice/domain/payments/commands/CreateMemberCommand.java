package com.hedvig.paymentservice.domain.payments.commands;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

public final class CreateMemberCommand {
  @TargetAggregateIdentifier
  private final
  String memberId;

  @java.beans.ConstructorProperties({"memberId"})
  public CreateMemberCommand(String memberId) {
    this.memberId = memberId;
  }

  public String getMemberId() {
    return this.memberId;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof CreateMemberCommand)) return false;
    final CreateMemberCommand other = (CreateMemberCommand) o;
    final Object this$memberId = this.getMemberId();
    final Object other$memberId = other.getMemberId();
    if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    return result;
  }

  public String toString() {
    return "CreateMemberCommand(memberId=" + this.getMemberId() + ")";
  }
}
