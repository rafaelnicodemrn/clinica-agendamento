package com.clinica.agendamento.service;

import com.clinica.agendamento.exception.NegocioException;
import com.clinica.agendamento.exception.RecursoNaoEncontradoException;
import com.clinica.agendamento.model.Profissional;
import com.clinica.agendamento.repository.ConsultaRepository;
import com.clinica.agendamento.repository.ProfissionalRepository;
import java.util.List;
import org.springframework.stereotype.Service;

// Regras do profissional e das especialidades
@Service
public class ProfissionalService {

    private final ProfissionalRepository repository;
    private final ConsultaRepository consultaRepository;

    public ProfissionalService(ProfissionalRepository repository, ConsultaRepository consultaRepository) {
        this.repository = repository;
        this.consultaRepository = consultaRepository;
    }

    // todos, ou só de uma especialidade (filtro da tela)
    public List<Profissional> listar(String especialidade) {
        if (especialidade == null || especialidade.isBlank()) {
            return repository.findAllByOrderByNome();
        }
        return repository.findByEspecialidadeIgnoreCaseOrderByNome(especialidade.trim());
    }

    // nomes das especialidades sem repetir, pro select do front
    public List<String> especialidades() {
        return repository.listarEspecialidades();
    }

    public Profissional buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado: " + id));
    }

    public Profissional criar(Profissional profissional) {
        // CRM tem que ser unico
        if (repository.existsByRegistro(profissional.getRegistro())) {
            throw new NegocioException("Já existe um profissional com este registro");
        }
        profissional.setId(null);
        return repository.save(profissional);
    }

    public Profissional atualizar(Long id, Profissional dados) {
        Profissional existente = buscar(id);
        if (repository.existsByRegistroAndIdNot(dados.getRegistro(), id)) {
            throw new NegocioException("Já existe um profissional com este registro");
        }
        existente.setNome(dados.getNome());
        existente.setEspecialidade(dados.getEspecialidade());
        existente.setRegistro(dados.getRegistro());
        existente.setEmail(dados.getEmail());
        existente.setAtivo(dados.isAtivo());
        return repository.save(existente);
    }

    public void excluir(Long id) {
        Profissional profissional = buscar(id);
        // se já atendeu alguem não apaga, o certo é deixar inativo
        if (consultaRepository.existsByProfissionalId(id)) {
            throw new NegocioException("Profissional possui consultas registradas; inative-o em vez de excluir");
        }
        repository.delete(profissional);
    }
}
