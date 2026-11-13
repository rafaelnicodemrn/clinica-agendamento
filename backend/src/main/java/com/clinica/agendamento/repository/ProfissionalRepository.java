package com.clinica.agendamento.repository;

import com.clinica.agendamento.model.Profissional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// Repositorio do profissional
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    // checagens do registro (CRM) unico, igual fizemos com o CPF do paciente
    boolean existsByRegistro(String registro);
    boolean existsByRegistroAndIdNot(String registro, Long id);

    List<Profissional> findAllByOrderByNome();
    List<Profissional> findByEspecialidadeIgnoreCaseOrderByNome(String especialidade);

    // Não tem tabela de especialidade, então pegamos os valores diferentes da coluna
    @Query("select distinct p.especialidade from Profissional p order by p.especialidade")
    List<String> listarEspecialidades();
}
