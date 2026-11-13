package com.clinica.agendamento.service;

import com.clinica.agendamento.exception.NegocioException;
import com.clinica.agendamento.model.Paciente;
import com.clinica.agendamento.repository.ConsultaRepository;
import com.clinica.agendamento.repository.PacienteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

// Regras do paciente.
// Caminho da requisicao: Controller -> Service -> Repository -> banco
// o controller so cuida do HTTP, quem decide se pode ou nao é aqui
@Service
public class PacienteService {

    private final PacienteRepository repository;
    // usado pra ver se o paciente tem consulta antes de deixar excluir
    private final ConsultaRepository consultaRepository;

    // injecao pelo construtor, o spring entrega os repositories prontos
    public PacienteService(PacienteRepository repository, ConsultaRepository consultaRepository) {
        this.repository = repository;
        this.consultaRepository = consultaRepository;
    }

    // sem busca traz todos por ordem de nome, com busca filtra pelo nome
    public List<Paciente> listar(String nome) {
        if (nome == null || nome.isBlank()) {
            return repository.findAllByOrderByNome();
        }
        return repository.findByNomeContainingIgnoreCaseOrderByNome(nome.trim());
    }

    // Optional = pode vir vazio se o id nao existir, o controller transforma isso em 404
    public Optional<Paciente> buscar(Long id) {
        return repository.findById(id);
    }

    public Paciente salvar(Paciente paciente) {
        // nao deixa dois pacientes com o mesmo cpf
        if (repository.existsByCpf(paciente.getCpf())) {
            throw new NegocioException("Já existe um paciente cadastrado com este CPF");
        }
        paciente.setId(null); // garante que vai inserir um novo
        return repository.save(paciente);
    }

    public Optional<Paciente> atualizar(Long id, Paciente dados) {
        Optional<Paciente> encontrado = repository.findById(id);
        if (encontrado.isEmpty()) {
            return Optional.empty();
        }
        // pode manter o proprio cpf, so nao pode usar o de outro paciente
        if (repository.existsByCpfAndIdNot(dados.getCpf(), id)) {
            throw new NegocioException("Já existe um paciente cadastrado com este CPF");
        }
        Paciente paciente = encontrado.get();
        paciente.setNome(dados.getNome());
        paciente.setCpf(dados.getCpf());
        paciente.setDataNascimento(dados.getDataNascimento());
        paciente.setTelefone(dados.getTelefone());
        paciente.setEmail(dados.getEmail());
        return Optional.of(repository.save(paciente));
    }

    // devolve false se nao achou o paciente (controller responde 404)
    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        // se ja tem consulta nao apaga, senao some o historico dele
        if (consultaRepository.existsByPacienteId(id)) {
            throw new NegocioException("Paciente possui consultas registradas e não pode ser excluído");
        }
        repository.deleteById(id);
        return true;
    }
}
