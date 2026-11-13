package com.clinica.agendamento.dto;

import com.clinica.agendamento.model.StatusConsulta;
import jakarta.validation.constraints.NotNull;

// Corpo do PATCH de status, ex: { "status": "CONFIRMADA" }
public record StatusRequest(@NotNull(message = "O status é obrigatório") StatusConsulta status) {
}
