package com.clinica.agendamento.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Aqui ficam os erros de todos os controllers num lugar so.
// O front sempre recebe o mesmo formato de JSON:
//   { "status": 409, "mensagem": "...", "campos": { ... } }
// e mostra a mensagem no alert da tela
@RestControllerAdvice
public class GlobalExceptionHandler {

    // regra de negocio quebrada -> 409 (conflito)
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, Object>> regraDeNegocio(NegocioException ex) {
        return erro(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // falhou no @Valid -> 400, mandando a mensagem de cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    // json quebrado ou com valor errado (ex: data invalida) -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Requisição inválida", null);
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem, Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", status.value());
        corpo.put("mensagem", mensagem);
        if (campos != null) {
            corpo.put("campos", campos);
        }
        return ResponseEntity.status(status).body(corpo);
    }
}
