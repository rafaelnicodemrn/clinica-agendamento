package com.clinica.agendamento.controller;

import com.clinica.agendamento.dto.ConsultaRequest;
import com.clinica.agendamento.dto.HorarioDisponivel;
import com.clinica.agendamento.dto.StatusRequest;
import com.clinica.agendamento.model.Consulta;
import com.clinica.agendamento.model.StatusConsulta;
import com.clinica.agendamento.service.ConsultaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Controller das consultas - terceira etapa (agenda e marcação)
// É o que junta paciente + profissional
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    // Lista com filtros opcionais, ex: /api/consultas?profissionalId=1&status=AGENDADA
    @GetMapping
    public List<Consulta> listar(@RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) StatusConsulta status) {
        return service.listar(profissionalId, pacienteId, status);
    }

    // Agenda do dia de um profissional: /api/consultas/agenda?profissionalId=1&data=2026-11-23
    // devolve cada horario com disponivel true/false
    @GetMapping("/agenda")
    public List<HorarioDisponivel> agenda(@RequestParam Long profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.agenda(profissionalId, data);
    }

    @GetMapping("/{id}")
    public Consulta buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // Marca a consulta. Aqui recebe um ConsultaRequest (só os ids + data) e não a entidade inteira
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Consulta agendar(@Valid @RequestBody ConsultaRequest request) {
        return service.agendar(request);
    }

    // Remarcar = trocar data/hora (ou profissional) de uma consulta que ainda nao aconteceu
    @PutMapping("/{id}")
    public Consulta reagendar(@PathVariable Long id, @Valid @RequestBody ConsultaRequest request) {
        return service.reagendar(id, request);
    }

    // PATCH porque muda só um campo (o status): confirmar, realizada ou cancelar
    @PatchMapping("/{id}/status")
    public Consulta alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.alterarStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
