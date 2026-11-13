package com.clinica.agendamento.exception;

// Exceção nossa pra quando alguma regra do sistema é quebrada
// (ex: horario ja ocupado). O GlobalExceptionHandler transforma ela em HTTP 409
public class NegocioException extends RuntimeException {
    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
