package com.clinica.agendamento.controller;

import com.clinica.agendamento.model.Paciente;
import com.clinica.agendamento.service.PacienteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Controller dos pacientes - primeira etapa do fluxo (cadastro)
// Ele só recebe a requisição HTTP e chama o service, as regras ficam la no PacienteService
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    // GET /api/pacientes  ou  /api/pacientes?nome=maria  (busca da tela)
    @GetMapping
    public List<Paciente> listar(@RequestParam(required = false) String nome) {
        return service.listar(nome);
    }

    // GET /api/pacientes/1 -> usado quando abre a tela de editar
    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // POST cria um paciente novo. O @Valid roda as validações que estão na entidade
    // e devolve 201 (CREATED) se deu certo
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Paciente criar(@Valid @RequestBody Paciente paciente) {
        return service.criar(paciente);
    }

    // PUT atualiza os dados de um paciente que já existe
    @PutMapping("/{id}")
    public Paciente atualizar(@PathVariable Long id, @Valid @RequestBody Paciente paciente) {
        return service.atualizar(id, paciente);
    }

    // DELETE retorna 204 (sem conteudo). Se ele tiver consulta o service barra com 409
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
