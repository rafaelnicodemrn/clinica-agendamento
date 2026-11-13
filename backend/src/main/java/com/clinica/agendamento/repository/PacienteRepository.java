package com.clinica.agendamento.repository;

import com.clinica.agendamento.model.Paciente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository do paciente. O JpaRepository ja traz save, findAll, findById, deleteById...
// e os outros metodos o Spring Data monta o SQL sozinho pelo nome (query methods)
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findAllByOrderByNome();

    // busca pelo pedaço do nome sem ligar pra maiuscula/minuscula (tipo LIKE '%maria%')
    List<Paciente> findByNomeContainingIgnoreCaseOrderByNome(String nome);

    // pra checar cpf repetido no cadastro
    boolean existsByCpf(String cpf);

    // na edicao: mesmo cpf mas em OUTRO paciente
    boolean existsByCpfAndIdNot(String cpf, Long id);
}
