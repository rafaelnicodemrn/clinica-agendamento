package com.clinica.agendamento.exception;

// Exceção que a gente lança quando alguma regra do sistema é quebrada
// (cpf repetido, horario ocupado, etc). O GlobalExceptionHandler pega ela
// e devolve 409 com a mensagem pro front mostrar
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
