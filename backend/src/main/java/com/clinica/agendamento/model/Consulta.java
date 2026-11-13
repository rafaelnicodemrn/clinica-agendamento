package com.clinica.agendamento.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// Entidade Consulta (etapa 3) - é ela que liga as outras duas:
//   Paciente 1 ---- N Consulta N ---- 1 Profissional
// no banco vira as colunas paciente_id e profissional_id (chaves estrangeiras)
@Entity
@Table(name = "consulta")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER pra já vir o paciente junto, assim o JSON tem o nome dele pra mostrar na lista
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    // inicio da consulta
    @Column(nullable = false)
    private LocalDateTime dataHora;

    // todas as consultas tem 30 min (um bloco da agenda)
    @Column(nullable = false)
    private int duracaoMinutos = 30;

    // STRING pra gravar "AGENDADA" no banco em vez de 0, 1, 2... fica mais facil de ler
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status = StatusConsulta.AGENDADA;

    @Column(length = 500)
    private String observacoes;

    // horario que termina (inicio + 30min). É só um calculo, por isso o @JsonIgnore
    // pra não ir no JSON. Usamos pra ver se dois horarios batem
    @JsonIgnore
    public LocalDateTime getFim() { return dataHora.plusMinutes(duracaoMinutos); }

    // getters e setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Profissional getProfissional() { return profissional; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(int duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
