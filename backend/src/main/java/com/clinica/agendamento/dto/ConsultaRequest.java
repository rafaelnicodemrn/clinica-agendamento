package com.clinica.agendamento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

// O que o front manda pra marcar/remarcar consulta.
// Só os ids do paciente e do profissional, a data/hora e a observação -
// o service é que busca as entidades completas no banco.
// (record = classe simples só com os campos, o Java já gera construtor e getters)
public record ConsultaRequest(
        @NotNull(message = "O paciente é obrigatório") Long pacienteId,
        @NotNull(message = "O profissional é obrigatório") Long profissionalId,
        @NotNull(message = "A data e hora são obrigatórias") LocalDateTime dataHora,
        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres") String observacoes) {
}
