package com.clinica.agendamento.service;

import com.clinica.agendamento.exception.NegocioException;
import com.clinica.agendamento.model.Profissional;
import com.clinica.agendamento.repository.ConsultaRepository;
import com.clinica.agendamento.repository.ProfissionalRepository;
import java.util.List;
import java.util.Optional;
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

    // todos, ou so os de uma especialidade (filtro da tela)
    public List<Profissional> listar(String especialidade) {
        if (especialidade == null || especialidade.isBlank()) {
            return repository.findAllByOrderByNome();
        }
        return repository.findByEspecialidadeIgnoreCaseOrderByNome(especialidade.trim());
    }

    // nomes das especialidades sem repetir, pros selects do front
    public List<String> listarEspecialidades() {
        return repository.listarEspecialidades();
    }

    public Optional<Profissional> buscar(Long id) {
        return repository.findById(id);
    }

    public Profissional salvar(Profissional profissional) {
        // o CRM nao pode repetir
        if (repository.existsByRegistro(profissional.getRegistro())) {
            throw new NegocioException("Já existe um profissional com este registro");
        }
        profissional.setId(null);
        return repository.save(profissional);
    }

    public Optional<Profissional> atualizar(Long id, Profissional dados) {
        Optional<Profissional> encontrado = repository.findById(id);
        if (encontrado.isEmpty()) {
            return Optional.empty();
        }
        if (repository.existsByRegistroAndIdNot(dados.getRegistro(), id)) {
            throw new NegocioException("Já existe um profissional com este registro");
        }
        Profissional profissional = encontrado.get();
        profissional.setNome(dados.getNome());
        profissional.setEspecialidade(dados.getEspecialidade());
        profissional.setRegistro(dados.getRegistro());
        profissional.setEmail(dados.getEmail());
        profissional.setAtivo(dados.isAtivo());
        return Optional.of(repository.save(profissional));
    }

    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        // se ja atendeu alguem nao apaga, o certo é deixar ele inativo
        if (consultaRepository.existsByProfissionalId(id)) {
            throw new NegocioException("Profissional possui consultas registradas; inative-o em vez de excluir");
        }
        repository.deleteById(id);
        return true;
    }
}
