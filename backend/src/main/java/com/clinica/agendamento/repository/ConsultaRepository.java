package com.clinica.agendamento.repository;

import com.clinica.agendamento.model.Consulta;
import com.clinica.agendamento.model.StatusConsulta;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Acesso ao banco das consultas.
// O JpaRepository já dá pronto: save, findById, findAll, delete...
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Usados antes de excluir paciente/profissional (se tiver consulta não deixa)
    boolean existsByPacienteId(Long pacienteId);
    boolean existsByProfissionalId(Long profissionalId);

    // Lista geral, da mais nova pra mais antiga
    List<Consulta> findAllByOrderByDataHoraDesc();

    // Aqui o nome do metodo ia ficar enorme, então escrevemos a consulta em JPQL na mão.
    // Traz as consultas de um profissional num periodo (usamos o dia inteiro) e só nos status passados
    @Query("select c from Consulta c where c.profissional.id = :profissionalId "
            + "and c.status in :status and c.dataHora >= :inicio and c.dataHora < :fim order by c.dataHora")
    List<Consulta> buscarDoProfissionalNoPeriodo(@Param("profissionalId") Long profissionalId,
            @Param("status") List<StatusConsulta> status,
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Mesma ideia, mas pelo paciente (pra ele nao ter 2 consultas no mesmo horario)
    @Query("select c from Consulta c where c.paciente.id = :pacienteId "
            + "and c.status in :status and c.dataHora >= :inicio and c.dataHora < :fim order by c.dataHora")
    List<Consulta> buscarDoPacienteNoPeriodo(@Param("pacienteId") Long pacienteId,
            @Param("status") List<StatusConsulta> status,
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
