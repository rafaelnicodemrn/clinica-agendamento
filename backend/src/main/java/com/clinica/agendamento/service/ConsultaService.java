package com.clinica.agendamento.service;

import com.clinica.agendamento.dto.ConsultaRequest;
import com.clinica.agendamento.dto.HorarioDisponivel;
import com.clinica.agendamento.exception.NegocioException;
import com.clinica.agendamento.exception.RecursoNaoEncontradoException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Regras da consulta / agenda.
// Aqui é onde as 3 entidades se juntam: pra marcar usamos o PacienteService e o
// ProfissionalService pra buscar os dois lados e depois conferimos horario e conflito.
//
// Horario da clinica: seg a sex, 08:00-12:00 e 14:00-18:00, de 30 em 30 min (16 horarios por dia)
@Service
public class ConsultaService {

    public static final int DURACAO_MINUTOS = 30;

    // status que ocupam horario na agenda
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

    // lista as consultas, cada filtro é opcional (null = não filtra por ele)
    public List<Consulta> listar(Long profissionalId, Long pacienteId, StatusConsulta status) {
        return repository.findAllByOrderByDataHoraDesc().stream()
                .filter(c -> profissionalId == null || c.getProfissional().getId().equals(profissionalId))
                .filter(c -> pacienteId == null || c.getPaciente().getId().equals(pacienteId))
                .filter(c -> status == null || c.getStatus() == status)
                .toList();
    }

    public Consulta buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada: " + id));
    }

    // Monta a agenda do dia do profissional: todos os horarios e se cada um ta livre.
    // A tela de agendar usa isso pra desenhar os botões verdes (livre) e cinzas (ocupado)
    public List<HorarioDisponivel> agenda(Long profissionalId, LocalDate data) {
        profissionalService.buscar(profissionalId); // da 404 se o profissional nao existir

        // consultas ativas dele naquele dia (00:00 até 00:00 do dia seguinte)
        List<Consulta> ocupadas = repository.buscarDoProfissionalNoPeriodo(
                profissionalId, ATIVAS, data.atStartOfDay(), data.plusDays(1).atStartOfDay());

        LocalDateTime agora = LocalDateTime.now();
        List<HorarioDisponivel> grade = new ArrayList<>();
        for (LocalDateTime horario : horariosDoDia(data)) {
            // livre se ainda não passou e nenhuma consulta ta nesse horario
            boolean livre = horario.isAfter(agora)
                    && ocupadas.stream().noneMatch(c -> sobrepoe(c, horario, horario.plusMinutes(DURACAO_MINUTOS)));
            grade.add(new HorarioDisponivel(horario, livre));
        }
        return grade;
    }

    // Marcar consulta:
    // 1) busca paciente e profissional  2) profissional tem que estar ativo
    // 3) horario futuro e dentro da grade  4) sem conflito  5) salva como AGENDADA
    @Transactional
    public Consulta agendar(ConsultaRequest req) {
        Paciente paciente = pacienteService.buscar(req.pacienteId());
        Profissional profissional = profissionalService.buscar(req.profissionalId());
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

    // Remarcar: mesmas validações de agendar, só que passamos o id da consulta
    // pra ela ser ignorada no conflito (senão ela ia conflitar com ela mesma)
    @Transactional
    public Consulta reagendar(Long id, ConsultaRequest req) {
        Consulta consulta = buscar(id);
        if (!consulta.getStatus().ativa()) {
            throw new NegocioException("Apenas consultas agendadas ou confirmadas podem ser alteradas");
        }
        Paciente paciente = pacienteService.buscar(req.pacienteId());
        Profissional profissional = profissionalService.buscar(req.profissionalId());
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
        return repository.save(consulta);
    }

    // Troca o status (confirmar / realizada / cancelar)
    // - realizada e cancelada não mudam mais
    // - não volta pra AGENDADA por aqui (pra isso é remarcar)
    // - não marca como realizada uma consulta que ainda não aconteceu
    @Transactional
    public Consulta alterarStatus(Long id, StatusConsulta novo) {
        Consulta consulta = buscar(id);
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
        return repository.save(consulta);
    }

    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    // ---------------- metodos auxiliares (regras de horario) ----------------

    // Gera os horarios de atendimento de um dia. Fim de semana volta lista vazia.
    // ex: 08:00, 08:30 ... 11:30, 14:00, 14:30 ... 17:30
    static List<LocalDateTime> horariosDoDia(LocalDate data) {
        List<LocalDateTime> horarios = new ArrayList<>();
        if (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return horarios;
        }
        // manhã
        for (LocalTime t = LocalTime.of(8, 0); t.isBefore(LocalTime.of(12, 0)); t = t.plusMinutes(DURACAO_MINUTOS)) {
            horarios.add(data.atTime(t));
        }
        // tarde
        for (LocalTime t = LocalTime.of(14, 0); t.isBefore(LocalTime.of(18, 0)); t = t.plusMinutes(DURACAO_MINUTOS)) {
            horarios.add(data.atTime(t));
        }
        return horarios;
    }

    // o horario tem que ser no futuro e ser um dos horarios da grade
    private void validarHorario(LocalDateTime dataHora) {
        if (!dataHora.isAfter(LocalDateTime.now())) {
            throw new NegocioException("A consulta deve ser marcada para uma data e hora futuras");
        }
        if (!horariosDoDia(dataHora.toLocalDate()).contains(dataHora.withSecond(0).withNano(0))
                || dataHora.getSecond() != 0) {
            throw new NegocioException(
                    "Horário fora do atendimento (seg a sex, 08:00-12:00 e 14:00-18:00, de 30 em 30 minutos)");
        }
    }

    // ve se o profissional ou o paciente ja tem consulta ativa nesse horario
    // idIgnorado = id da consulta que ta sendo remarcada (null se for nova)
    private void validarConflitos(Long idIgnorado, Long pacienteId, Long profissionalId, LocalDateTime inicio) {
        LocalDateTime fim = inicio.plusMinutes(DURACAO_MINUTOS);
        // só precisa olhar as consultas do mesmo dia
        LocalDateTime janelaIni = inicio.toLocalDate().atStartOfDay();
        LocalDateTime janelaFim = janelaIni.plusDays(1);

        boolean profissionalOcupado = repository.buscarDoProfissionalNoPeriodo(profissionalId, ATIVAS, janelaIni, janelaFim)
                .stream().filter(c -> !c.getId().equals(idIgnorado)).anyMatch(c -> sobrepoe(c, inicio, fim));
        if (profissionalOcupado) {
            throw new NegocioException("O profissional já possui consulta neste horário");
        }

        boolean pacienteOcupado = repository.buscarDoPacienteNoPeriodo(pacienteId, ATIVAS, janelaIni, janelaFim)
                .stream().filter(c -> !c.getId().equals(idIgnorado)).anyMatch(c -> sobrepoe(c, inicio, fim));
        if (pacienteOcupado) {
            throw new NegocioException("O paciente já possui consulta neste horário");
        }
    }

    // dois horarios batem quando um começa antes do outro terminar e termina depois do outro começar
    //   consulta: 09:00 a 09:30
    //   pedido 09:15 a 09:45 -> bate
    //   pedido 09:30 a 10:00 -> não bate (começa quando a outra acaba)
    private static boolean sobrepoe(Consulta c, LocalDateTime inicio, LocalDateTime fim) {
        return c.getDataHora().isBefore(fim) && c.getFim().isAfter(inicio);
    }
}
