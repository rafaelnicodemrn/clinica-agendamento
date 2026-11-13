package com.clinica.agendamento.service;

import com.clinica.agendamento.dto.ConsultaRequest;
import com.clinica.agendamento.dto.HorarioDisponivel;
import com.clinica.agendamento.exception.NegocioException;
import com.clinica.agendamento.model.Consulta;
import com.clinica.agendamento.model.Paciente;
import com.clinica.agendamento.model.Profissional;
import com.clinica.agendamento.model.StatusConsulta;
import com.clinica.agendamento.repository.ConsultaRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Regras da consulta / agenda.
// É aqui que as 3 entidades se encontram: pra marcar a consulta usamos o
// PacienteService e o ProfissionalService pra achar os dois lados, e depois
// conferimos se o horario é valido e se nao bate com outra consulta.
//
// Horario da clinica: seg a sex, 08:00-12:00 e 14:00-18:00, de 30 em 30 min
// (16 horarios por dia)
@Service
public class ConsultaService {

    public static final int DURACAO_MINUTOS = 30;

    // so esses status ocupam horario na agenda
    private static final List<StatusConsulta> ATIVAS = List.of(StatusConsulta.AGENDADA, StatusConsulta.CONFIRMADA);

    private final ConsultaRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public ConsultaService(ConsultaRepository repository, PacienteService pacienteService,
            ProfissionalService profissionalService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
    }

    // lista as consultas, os filtros sao opcionais (null = nao filtra)
    public List<Consulta> listar(Long profissionalId, Long pacienteId, StatusConsulta status) {
        return repository.findAllByOrderByDataHoraDesc().stream()
                .filter(c -> profissionalId == null || c.getProfissional().getId().equals(profissionalId))
                .filter(c -> pacienteId == null || c.getPaciente().getId().equals(pacienteId))
                .filter(c -> status == null || c.getStatus() == status)
                .toList();
    }

    public Optional<Consulta> buscar(Long id) {
        return repository.findById(id);
    }

    // Monta a agenda do dia: todos os horarios do profissional e se cada um ta livre.
    // A tela de agendar usa isso pra pintar os botoes (verde livre, cinza ocupado)
    public List<HorarioDisponivel> agenda(Long profissionalId, LocalDate data) {
        acharProfissional(profissionalId);

        // consultas ativas dele naquele dia (das 00:00 ate 00:00 do dia seguinte)
        List<Consulta> ocupadas = repository.buscarDoProfissionalNoPeriodo(
                profissionalId, ATIVAS, data.atStartOfDay(), data.plusDays(1).atStartOfDay());

        LocalDateTime agora = LocalDateTime.now();
        List<HorarioDisponivel> grade = new ArrayList<>();
        for (LocalDateTime horario : horariosDoDia(data)) {
            // livre = ainda nao passou e ninguem marcou nesse horario
            boolean livre = horario.isAfter(agora)
                    && ocupadas.stream().noneMatch(c -> sobrepoe(c, horario, horario.plusMinutes(DURACAO_MINUTOS)));
            grade.add(new HorarioDisponivel(horario, livre));
        }
        return grade;
    }

    // Marcar consulta, passo a passo:
    // 1) acha paciente e profissional   2) profissional tem que estar ativo
    // 3) horario futuro e dentro da grade   4) sem conflito   5) salva como AGENDADA
    @Transactional
    public Consulta agendar(ConsultaRequest req) {
        Paciente paciente = acharPaciente(req.pacienteId());
        Profissional profissional = acharProfissional(req.profissionalId());
        if (!profissional.isAtivo()) {
            throw new NegocioException("O profissional está inativo e não aceita novos agendamentos");
        }
        validarHorario(req.dataHora());
        validarConflitos(null, paciente.getId(), profissional.getId(), req.dataHora());

        Consulta consulta = new Consulta();
        consulta.setPaciente(paciente);
        consulta.setProfissional(profissional);
        consulta.setDataHora(req.dataHora());
        consulta.setDuracaoMinutos(DURACAO_MINUTOS);
        consulta.setObservacoes(req.observacoes());
        consulta.setStatus(StatusConsulta.AGENDADA);
        return repository.save(consulta);
    }

    // Remarcar: mesmas validacoes do agendar, mas passamos o id da consulta
    // pra ela ser ignorada no conflito (senao ia conflitar com ela mesma)
    @Transactional
    public Optional<Consulta> reagendar(Long id, ConsultaRequest req) {
        Optional<Consulta> encontrada = repository.findById(id);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        Consulta consulta = encontrada.get();
        if (!consulta.getStatus().ativa()) {
            throw new NegocioException("Apenas consultas agendadas ou confirmadas podem ser alteradas");
        }
        Paciente paciente = acharPaciente(req.pacienteId());
        Profissional profissional = acharProfissional(req.profissionalId());
        if (!profissional.isAtivo()) {
            throw new NegocioException("O profissional está inativo e não aceita novos agendamentos");
        }
        validarHorario(req.dataHora());
        validarConflitos(id, paciente.getId(), profissional.getId(), req.dataHora());

        consulta.setPaciente(paciente);
        consulta.setProfissional(profissional);
        consulta.setDataHora(req.dataHora());
        consulta.setObservacoes(req.observacoes());
        consulta.setStatus(StatusConsulta.AGENDADA); // remarcou, volta pra agendada
        return Optional.of(repository.save(consulta));
    }

    // Troca o status (confirmar, realizada ou cancelar)
    // - realizada e cancelada sao finais, nao mudam mais
    // - nao volta pra AGENDADA por aqui (pra isso tem o remarcar)
    // - nao da pra marcar como realizada uma consulta que ainda nao aconteceu
    @Transactional
    public Optional<Consulta> alterarStatus(Long id, StatusConsulta novo) {
        Optional<Consulta> encontrada = repository.findById(id);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        Consulta consulta = encontrada.get();
        StatusConsulta atual = consulta.getStatus();
        if (!atual.ativa()) {
            throw new NegocioException("Consulta " + atual.name().toLowerCase() + " não pode mais ser alterada");
        }
        if (novo == atual || novo == StatusConsulta.AGENDADA) {
            throw new NegocioException("Transição de status inválida: " + atual + " -> " + novo);
        }
        if (novo == StatusConsulta.REALIZADA && consulta.getDataHora().isAfter(LocalDateTime.now())) {
            throw new NegocioException("Não é possível marcar como realizada uma consulta futura");
        }
        consulta.setStatus(novo);
        return Optional.of(repository.save(consulta));
    }

    public boolean excluir(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    // ---------------- metodos de apoio ----------------

    // aqui o paciente/profissional vem dentro do JSON da consulta, entao se o id
    // nao existir é um erro nos dados enviados e nao um 404 da consulta
    private Paciente acharPaciente(Long id) {
        return pacienteService.buscar(id)
                .orElseThrow(() -> new NegocioException("Paciente não encontrado"));
    }

    private Profissional acharProfissional(Long id) {
        return profissionalService.buscar(id)
                .orElseThrow(() -> new NegocioException("Profissional não encontrado"));
    }

    // gera os horarios de atendimento do dia, fim de semana volta vazio
    // ex: 08:00, 08:30 ... 11:30, 14:00, 14:30 ... 17:30
    static List<LocalDateTime> horariosDoDia(LocalDate data) {
        List<LocalDateTime> horarios = new ArrayList<>();
        if (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return horarios;
        }
        // manha
        for (LocalTime t = LocalTime.of(8, 0); t.isBefore(LocalTime.of(12, 0)); t = t.plusMinutes(DURACAO_MINUTOS)) {
            horarios.add(data.atTime(t));
        }
        // tarde
        for (LocalTime t = LocalTime.of(14, 0); t.isBefore(LocalTime.of(18, 0)); t = t.plusMinutes(DURACAO_MINUTOS)) {
            horarios.add(data.atTime(t));
        }
        return horarios;
    }

    // o horario tem que ser no futuro e ser exatamente um dos horarios da grade
    private void validarHorario(LocalDateTime dataHora) {
        if (!dataHora.isAfter(LocalDateTime.now())) {
            throw new NegocioException("A consulta deve ser marcada para uma data e hora futuras");
        }
        if (!horariosDoDia(dataHora.toLocalDate()).contains(dataHora)) {
            throw new NegocioException(
                    "Horário fora do atendimento (seg a sex, 08:00-12:00 e 14:00-18:00, de 30 em 30 minutos)");
        }
    }

    // ve se o profissional ou o paciente ja tem consulta ativa nesse horario.
    // idIgnorado = id da consulta que ta sendo remarcada (null quando é nova)
    private void validarConflitos(Long idIgnorado, Long pacienteId, Long profissionalId, LocalDateTime inicio) {
        LocalDateTime fim = inicio.plusMinutes(DURACAO_MINUTOS);
        // so precisa olhar as consultas do mesmo dia
        LocalDateTime comecoDoDia = inicio.toLocalDate().atStartOfDay();
        LocalDateTime fimDoDia = comecoDoDia.plusDays(1);

        boolean profissionalOcupado = repository
                .buscarDoProfissionalNoPeriodo(profissionalId, ATIVAS, comecoDoDia, fimDoDia).stream()
                .filter(c -> !c.getId().equals(idIgnorado))
                .anyMatch(c -> sobrepoe(c, inicio, fim));
        if (profissionalOcupado) {
            throw new NegocioException("O profissional já possui consulta neste horário");
        }

        boolean pacienteOcupado = repository
                .buscarDoPacienteNoPeriodo(pacienteId, ATIVAS, comecoDoDia, fimDoDia).stream()
                .filter(c -> !c.getId().equals(idIgnorado))
                .anyMatch(c -> sobrepoe(c, inicio, fim));
        if (pacienteOcupado) {
            throw new NegocioException("O paciente já possui consulta neste horário");
        }
    }

    // dois horarios batem quando um comeca antes do outro terminar e termina depois do outro comecar
    //   consulta marcada: 09:00 a 09:30
    //   pedido 09:15 a 09:45 -> bate
    //   pedido 09:30 a 10:00 -> nao bate (comeca bem quando a outra acaba)
    private static boolean sobrepoe(Consulta c, LocalDateTime inicio, LocalDateTime fim) {
        return c.getDataHora().isBefore(fim) && c.getFim().isAfter(inicio);
    }
}
