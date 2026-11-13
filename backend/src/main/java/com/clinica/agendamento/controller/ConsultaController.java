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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller das consultas - etapa 3 (agenda e marcacao)
// é o que junta o paciente com o profissional
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    // lista com filtros opcionais, ex: /api/consultas?profissionalId=1&status=AGENDADA
    @GetMapping
    public List<Consulta> listar(@RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) StatusConsulta status) {
        return service.listar(profissionalId, pacienteId, status);
    }

    // agenda do dia: /api/consultas/agenda?profissionalId=1&data=2026-11-23
    // volta cada horario com disponivel true/false
    @GetMapping("/agenda")
    public List<HorarioDisponivel> agenda(@RequestParam Long profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.agenda(profissionalId, data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consulta> buscar(@PathVariable Long id) {
        return service.buscar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // marcar consulta. Aqui o corpo é um ConsultaRequest (so os ids + data),
    // nao a entidade inteira
    @PostMapping
    public ResponseEntity<Consulta> agendar(@Valid @RequestBody ConsultaRequest request) {
        Consulta salva = service.agendar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    // remarcar = trocar a data/hora (ou o profissional) de uma consulta que ainda nao aconteceu
    @PutMapping("/{id}")
    public ResponseEntity<Consulta> reagendar(@PathVariable Long id, @Valid @RequestBody ConsultaRequest request) {
        return service.reagendar(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PATCH porque muda so um campo, o status (confirmar, realizada, cancelar)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Consulta> alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.alterarStatus(id, request.status())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!service.excluir(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
