package com.salgaki.service.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String mensagem) {
        super(mensagem);
    }
}
