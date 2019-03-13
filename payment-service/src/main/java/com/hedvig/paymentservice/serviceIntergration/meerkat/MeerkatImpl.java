package com.hedvig.paymentservice.serviceIntergration.meerkat;

import com.hedvig.paymentservice.serviceIntergration.meerkat.dto.MeerkatResponse;
import com.hedvig.paymentservice.serviceIntergration.memberService.dto.SanctionStatus;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
public class MeerkatImpl implements Meerkat {

  private MeerkatClient meerkatClient;

  public MeerkatImpl(MeerkatClient meerkatClient) {
    this.meerkatClient = meerkatClient;
  }

  @Override
  public SanctionStatus getMemberSanctionStatus(String fullName) {
    try {
      ResponseEntity<MeerkatResponse> response = meerkatClient.getSanctionListStatus(fullName);

      if (response.getStatusCode().is2xxSuccessful()) {
        return Objects.requireNonNull(response.getBody()).getResult();
      }
      return SanctionStatus.Undetermined;
    } catch (RestClientResponseException ex) {
      log.error("Could not check sanction list for memberId {} , {}", fullName, ex);
      return SanctionStatus.Undetermined;
    } catch (NullPointerException ex){
      log.error("Could not check sanction list, response null for memberId {} , {}", fullName, ex);
      return SanctionStatus.Undetermined;
    }
  }
}
