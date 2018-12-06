package com.hedvig.paymentservice.serviceIntergration.meerkat;

import com.hedvig.paymentservice.configuration.FeignConfiguration;
import com.hedvig.paymentservice.serviceIntergration.meerkat.dto.MeerkatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(
    name = "meerkatClient",
    url = "${hedvig.meerkat.url:meerkat}",
    configuration = FeignConfiguration.class
)
public interface MeerkatClient {

  @RequestMapping(value = "/api/check?query={query}", method = RequestMethod.GET)
  ResponseEntity<MeerkatResponse> getSanctionListStatus(@PathVariable("query") String query);
}
