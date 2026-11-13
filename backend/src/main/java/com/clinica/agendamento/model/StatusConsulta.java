package com.clinica.agendamento.model;

// Os status que uma consulta pode ter:
// AGENDADA -> CONFIRMADA -> REALIZADA
//          \-> CANCELADA
// realizada e cancelada são finais, depois disso não muda mais
public enum StatusConsulta {
    AGENDADA, CONFIRMADA, REALIZADA, CANCELADA;

    // "ativa" = ainda vai acontecer, então ocupa o horario na agenda
    public boolean ativa() {
        return this == AGENDADA || this == CONFIRMADA;
    }
}
