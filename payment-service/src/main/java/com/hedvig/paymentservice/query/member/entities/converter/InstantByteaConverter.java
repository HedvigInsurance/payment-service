package com.hedvig.paymentservice.query.member.entities.converter;

import java.time.Instant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

@Converter
@Slf4j
public class InstantByteaConverter implements AttributeConverter<Instant, Byte[]> {

  @SneakyThrows
  @Override
  public Byte[] convertToDatabaseColumn(final Instant instant) {
    if(instant == null) {
      return null;
    }
    try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(buffer)) {
      oos.writeObject(instant);
      return ArrayUtils.toObject(buffer.toByteArray());
    }catch (Exception e){
      log.error("Error occurred converting ldt to byte[], e.message: [{}], e.cause: [{}], e.stack: [{}], e" +
              ".class: [{}]",
          e.getMessage(), e.getCause(), e.getStackTrace(), e.getClass());
      throw e;
    }
  }

  @SneakyThrows
  @Override
  public Instant convertToEntityAttribute(final Byte[] bytes) {
    if(bytes == null) {
      return null;
    }
    try(final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(ArrayUtils.toPrimitive(bytes));
        final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)){
      final Instant instant = Instant.parse(objectInputStream.readObject().toString());
      log.info("Object: [{}]", instant);
      return  instant;
    }catch (Exception e){
      log.error("Error occurred converting byte[] to ldt, e.message: [{}], e.cause: [{}], e.stack: [{}], e" +
              ".class: [{}]",
          e.getMessage(), e.getCause(), e.getStackTrace(), e.getClass());
      throw e;
    }

  }
}