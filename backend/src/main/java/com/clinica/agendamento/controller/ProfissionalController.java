package com.clinica.agendamento.controller;

import com.clinica.agendamento.model.Profissional;
import com.clinica.agendamento.service.ProfissionalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Controller dos profissionais - segunda etapa (lista de profissionais/especialidades)
@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    // GET /api/profissionais?especialidade=Cardiologia  (sem o parametro traz todos)
    @GetMapping
    public List<Profissional> listar(@RequestParam(required = false) String especialidade) {
        return service.listar(especialidade);
    }

    // Lista só os nomes das especialidades, pra preencher o select do filtro no front
    // obs: tem que ficar antes do /{id}, se não o spring tentaria converter "especialidades" em numero
    @GetMapping("/especialidades")
    public List<String> especialidades() {
        return service.especialidades();
    }

    @GetMapping("/{id}")
    public Profissional buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Profissional criar(@Valid @RequestBody Profissional profissional) {
        return service.criar(profissional);
    }

    @PutMapping("/{id}")
    public Profissional atualizar(@PathVariable Long id, @Valid @RequestBody Profissional profissional) {
        return service.atualizar(id, profissional);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
