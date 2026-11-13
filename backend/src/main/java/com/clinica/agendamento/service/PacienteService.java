package com.clinica.agendamento.service;

import com.clinica.agendamento.exception.NegocioException;
import com.clinica.agendamento.exception.RecursoNaoEncontradoException;
import com.clinica.agendamento.model.Paciente;
import com.clinica.agendamento.repository.ConsultaRepository;
import com.clinica.agendamento.repository.PacienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

// Regras do paciente.
// Fluxo das camadas: Controller (HTTP) -> Service (regras) -> Repository (banco)
@Service
public class PacienteService {

    private final PacienteRepository repository;
    // precisa do repo de consultas pra checar se pode excluir
    private final ConsultaRepository consultaRepository;

    // o Spring injeta os repositorios pelo construtor
    public PacienteService(PacienteRepository repository, ConsultaRepository consultaRepository) {
        this.repository = repository;
        this.consultaRepository = consultaRepository;
    }

    // sem busca traz todos em ordem alfabetica, com busca filtra pelo nome
    public List<Paciente> listar(String nome) {
        if (nome == null || nome.isBlank()) {
            return repository.findAll().stream()
                    .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome())).toList();
        }
        return repository.findByNomeContainingIgnoreCaseOrderByNome(nome.trim());
    }

    // se o id não existir solta a exceção, que vira 404
    public Paciente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado: " + id));
    }

    public Paciente criar(Paciente paciente) {
        // não deixa cadastrar dois com o mesmo CPF
        if (repository.existsByCpf(paciente.getCpf())) {
            throw new NegocioException("Já existe um paciente cadastrado com este CPF");
        }
        paciente.setId(null); // garante que vai criar um novo, mesmo se vier id no JSON
        return repository.save(paciente);
    }

    public Paciente atualizar(Long id, Paciente dados) {
        Paciente existente = buscar(id);
        // na edição pode manter o proprio CPF, só não pode usar o de outro paciente
        if (repository.existsByCpfAndIdNot(dados.getCpf(), id)) {
            throw new NegocioException("Já existe um paciente cadastrado com este CPF");
        }
        existente.setNome(dados.getNome());
        existente.setCpf(dados.getCpf());
        existente.setDataNascimento(dados.getDataNascimento());
        existente.setTelefone(dados.getTelefone());
        existente.setEmail(dados.getEmail());
        return repository.save(existente);
    }

    public void excluir(Long id) {
        Paciente paciente = buscar(id);
        // se ja tem consulta não pode apagar, senão perde o historico
        if (consultaRepository.existsByPacienteId(id)) {
            throw new NegocioException("Paciente possui consultas registradas e não pode ser excluído");
        }
        repository.delete(paciente);
    }
}
