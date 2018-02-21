package com.hedvig.paymentservice.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Member not found")
public class MemberNotFoundException extends RuntimeException {

}
