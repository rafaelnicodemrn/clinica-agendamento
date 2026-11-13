package com.clinica.agendamento.exception;

// Lançada quando busca um id que não existe no banco -> vira HTTP 404
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
