package com.clinica.agendamento.dto;

import java.time.LocalDateTime;

// Um "quadradinho" da agenda: o horario e se ele esta livre ou não
public record HorarioDisponivel(LocalDateTime dataHora, boolean disponivel) {
}
