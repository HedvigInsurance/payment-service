package com.hedvig.paymentservice.configuration;

import com.hedvig.paymentService.trustly.NotificationHandler;
import com.hedvig.paymentService.trustly.SignedAPI;
import java.net.URISyntaxException;
import java.security.Security;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
class Trustly {

    @Value("${hedvig.trustly.privateKeyPath}")
    String privateKeyPath;

    @Value("${hedvig.trustly.privateKeyPassword}")
    String privateKeyPassword;

    @Value("${hedvig.trustly.username}")
    String usernamePremium;

    @Value("${hedvig.trustly.password}")
    String passwordPremium;

    @Value("${hedvig.trustly.claim.hdi.username}")
    String usernameClaimHdi;

    @Value("${hedvig.trustly.claim.hdi.password}")
    String passwordClaimHdi;

    @Value("${hedvig.trustly.claim.hedvig.username}")
    String usernameClaimHedvig;

    @Value("${hedvig.trustly.claim.hedvig.password}")
    String passwordClaimHedvig;

    @Autowired
    Environment environment;

    @Bean
    SignedAPI createSignedApi() throws URISyntaxException {
        Security.addProvider(new BouncyCastleProvider());
        SignedAPI api = new SignedAPI();
        boolean testEnvironment = !ArrayUtils.contains(environment.getActiveProfiles(), "production");
        api.init(
            privateKeyPath,
            privateKeyPassword,
            usernamePremium,
            passwordPremium,
            usernameClaimHdi,
            passwordClaimHdi,
            usernameClaimHedvig,
            passwordClaimHedvig,
            testEnvironment
        );

        return api;
    }

    @Bean
    NotificationHandler createNotificationHandler() {
        return new NotificationHandler();
    }
}
