package com.clinica.agendamento.repository;

import com.clinica.agendamento.model.Paciente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio do paciente. O Spring Data cria o SQL sozinho a partir do nome do metodo
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // select count(*) > 0 from paciente where cpf = ?
    boolean existsByCpf(String cpf);

    // mesmo CPF mas em OUTRO paciente (usado na edição)
    boolean existsByCpfAndIdNot(String cpf, Long id);

    // busca parcial pelo nome, sem diferenciar maiuscula/minuscula (tipo um LIKE %nome%)
    List<Paciente> findByNomeContainingIgnoreCaseOrderByNome(String nome);
}
