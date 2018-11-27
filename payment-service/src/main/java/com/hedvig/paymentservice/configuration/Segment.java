package com.hedvig.paymentservice.configuration;

import com.segment.analytics.Analytics;
import com.segment.analytics.Log;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Segment {

    private static Logger log = LoggerFactory.getLogger(Segment.class);

    @Bean
    public Analytics segmentAnalytics(@Value("${hedvig.segment.writeKey}") String  writeKey) {
        return Analytics.builder(writeKey).log(new Log(){

            @Override
            public void print(Level level, String format, Object... args) {
                val msg = String.format(format, args);
                switch (level) {
                    case DEBUG:
                        log.debug(msg);
                        break;
                    case VERBOSE:
                        log.info(msg);
                        break;
                    case ERROR:
                        log.error(msg);
                }
            }

            @Override
            public void print(Level level, Throwable error, String format, Object... args) {
                val msg = String.format(format, args);
                switch (level) {
                    case DEBUG:
                        log.debug(msg, error);
                        break;
                    case VERBOSE:
                        log.info(msg, error);
                        break;
                    case ERROR:
                        log.error(msg, error);
                }
            }
        }).build();
    }


}
