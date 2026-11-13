package com.clinica.agendamento.controller;

import com.clinica.agendamento.model.Paciente;
import com.clinica.agendamento.service.PacienteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller dos pacientes - etapa 1 do fluxo (cadastro)
// so recebe a requisicao, chama o service e monta a resposta HTTP.
// As regras ficam no PacienteService
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    // GET /api/pacientes   ou   /api/pacientes?nome=maria
    @GetMapping
    public List<Paciente> listar(@RequestParam(required = false) String nome) {
        return service.listar(nome);
    }

    // GET /api/pacientes/1 -> 200 com o paciente ou 404 se nao existir
    @GetMapping("/{id}")
    public ResponseEntity<Paciente> buscar(@PathVariable Long id) {
        return service.buscar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST -> 201 Created. O @Valid confere as anotacoes da entidade antes de chegar no service
    @PostMapping
    public ResponseEntity<Paciente> criar(@Valid @RequestBody Paciente paciente) {
        Paciente salvo = service.salvar(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // PUT /api/pacientes/1 -> o id vem na url e os dados novos no corpo
    @PutMapping("/{id}")
    public ResponseEntity<Paciente> atualizar(@PathVariable Long id, @Valid @RequestBody Paciente dados) {
        return service.atualizar(id, dados)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE -> 204 se apagou, 404 se nao achou (e 409 se tiver consulta, la no service)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!service.excluir(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
