package com.clinica.agendamento.controller;

import com.clinica.agendamento.model.Profissional;
import com.clinica.agendamento.service.ProfissionalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller dos profissionais - etapa 2 (lista de profissionais e especialidades)
@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    // GET /api/profissionais?especialidade=Cardiologia (sem parametro traz todos)
    @GetMapping
    public List<Profissional> listar(@RequestParam(required = false) String especialidade) {
        return service.listar(especialidade);
    }

    // so os nomes das especialidades, pro select de filtro do front.
    // fica antes do /{id} pra nao confundir "especialidades" com um id
    @GetMapping("/especialidades")
    public List<String> listarEspecialidades() {
        return service.listarEspecialidades();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profissional> buscar(@PathVariable Long id) {
        return service.buscar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Profissional> criar(@Valid @RequestBody Profissional profissional) {
        Profissional salvo = service.salvar(profissional);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profissional> atualizar(@PathVariable Long id, @Valid @RequestBody Profissional dados) {
        return service.atualizar(id, dados)
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
