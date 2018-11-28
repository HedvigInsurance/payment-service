package com.hedvig.paymentservice.serviceIntergration.meerkat;

import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;

public interface Meerkat {
  SanctionStatus getMemberSanctionStatus(String fullName);
}
