package com.clinica.agendamento.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Trata os erros de TODOS os controllers num lugar só.
// Assim o front sempre recebe um JSON no mesmo formato:
//   { "status": 409, "mensagem": "...", "campos": { ... } }
// e consegue mostrar a mensagem no alert vermelho da tela
@RestControllerAdvice
public class GlobalExceptionHandler {

    // id que não existe -> 404
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // quebrou alguma regra (CPF repetido, horario ocupado...) -> 409 Conflict
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, Object>> negocio(NegocioException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // falhou no @Valid -> 400, e manda a mensagem de cada campo que deu erro
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    // JSON mal formado ou parametro com tipo errado (ex: data invalida) -> 400
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> requisicaoInvalida(Exception ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida", null);
    }

    // monta o corpo da resposta de erro
    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem, Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", status.value());
        corpo.put("mensagem", mensagem);
        if (campos != null) {
            corpo.put("campos", campos);
        }
        return ResponseEntity.status(status).body(corpo);
    }
}
