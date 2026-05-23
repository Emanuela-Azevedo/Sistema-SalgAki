package com.salgaki.service.exception;

public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(int saldoAtual) {
        super("Estoque insuficiente. Saldo atual: " + saldoAtual);
    }
}
