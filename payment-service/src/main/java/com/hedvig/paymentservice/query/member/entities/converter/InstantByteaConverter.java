package com.hedvig.paymentservice.query.member.entities.converter;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;

@Converter
public class InstantByteaConverter implements AttributeConverter<Instant, Byte[]> {

    private static final Logger log = LoggerFactory.getLogger(InstantByteaConverter.class);

    @Override
    public Byte[] convertToDatabaseColumn(final Instant instant){
        if (instant == null) {
            return null;
        }
        try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             final ObjectOutputStream oos = new ObjectOutputStream(buffer)) {
            oos.writeObject(instant);
            return ArrayUtils.toObject(buffer.toByteArray());
        } catch (Exception e) {
            log.error("Error occurred converting ldt to byte[], e.message: [{}], e.cause: [{}], e.stack: [{}], e" +
                    ".class: [{}]",
                e.getMessage(), e.getCause(), e.getStackTrace(), e.getClass());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Instant convertToEntityAttribute(final Byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(ArrayUtils.toPrimitive(bytes));
             final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            final Instant instant = Instant.parse(objectInputStream.readObject().toString());
            log.info("Object: [{}]", instant);
            return instant;
        } catch (Exception e) {
            log.error("Error occurred converting byte[] to ldt, e.message: [{}], e.cause: [{}], e.stack: [{}], e" +
                    ".class: [{}]",
                e.getMessage(), e.getCause(), e.getStackTrace(), e.getClass());
            throw new RuntimeException(e);
        }

    }
}
