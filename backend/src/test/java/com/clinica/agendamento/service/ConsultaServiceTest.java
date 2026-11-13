package com.clinica.agendamento.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Testes das regras de agendamento (sem banco: o repository e os outros services
// sao "falsos", feitos com Mockito, assim testamos so a logica do ConsultaService)
class ConsultaServiceTest {

    private ConsultaRepository repository;
    private PacienteService pacienteService;
    private ProfissionalService profissionalService;
    private ConsultaService service;
    private Paciente paciente;
    private Profissional profissional;

    @BeforeEach
    void setUp() {
        repository = mock(ConsultaRepository.class);
        pacienteService = mock(PacienteService.class);
        profissionalService = mock(ProfissionalService.class);
        service = new ConsultaService(repository, pacienteService, profissionalService);

        paciente = new Paciente();
        paciente.setId(1L);
        profissional = new Profissional();
        profissional.setId(2L);
        profissional.setAtivo(true);
        when(pacienteService.buscar(1L)).thenReturn(Optional.of(paciente));
        when(profissionalService.buscar(2L)).thenReturn(Optional.of(profissional));
        when(repository.save(any(Consulta.class))).thenAnswer(i -> i.getArgument(0));
        when(repository.buscarDoProfissionalNoPeriodo(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.buscarDoPacienteNoPeriodo(any(), any(), any(), any())).thenReturn(List.of());
    }

    // proximo dia de semana a partir de amanha, pra o teste nao cair em sabado/domingo
    private LocalDateTime proximoDiaUtil(int hora, int minuto) {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d.atTime(LocalTime.of(hora, minuto));
    }

    private Consulta consultaExistente(long id, LocalDateTime inicio) {
        Consulta c = new Consulta();
        c.setId(id);
        c.setPaciente(paciente);
        c.setProfissional(profissional);
        c.setDataHora(inicio);
        c.setStatus(StatusConsulta.AGENDADA);
        return c;
    }

    @Test
    void agendaConsultaEmHorarioLivre() {
        Consulta c = service.agendar(new ConsultaRequest(1L, 2L, proximoDiaUtil(9, 0), "retorno"));
        assertEquals(StatusConsulta.AGENDADA, c.getStatus());
        assertEquals(30, c.getDuracaoMinutos());
    }

    @Test
    void rejeitaDataNoPassado() {
        var req = new ConsultaRequest(1L, 2L, LocalDateTime.now().minusDays(1).withHour(9).withMinute(0), null);
        assertThrows(NegocioException.class, () -> service.agendar(req));
    }

    @Test
    void rejeitaHorarioForaDoAtendimento() {
        assertThrows(NegocioException.class,
                () -> service.agendar(new ConsultaRequest(1L, 2L, proximoDiaUtil(13, 0), null)));
        assertThrows(NegocioException.class,
                () -> service.agendar(new ConsultaRequest(1L, 2L, proximoDiaUtil(9, 10), null)));
    }

    @Test
    void rejeitaConflitoDoProfissional() {
        LocalDateTime horario = proximoDiaUtil(10, 0);
        when(repository.buscarDoProfissionalNoPeriodo(eq(2L), any(), any(), any()))
                .thenReturn(List.of(consultaExistente(99L, horario)));
        var ex = assertThrows(NegocioException.class,
                () -> service.agendar(new ConsultaRequest(1L, 2L, horario, null)));
        assertTrue(ex.getMessage().contains("profissional"));
    }

    @Test
    void rejeitaConflitoDoPaciente() {
        LocalDateTime horario = proximoDiaUtil(10, 30);
        when(repository.buscarDoPacienteNoPeriodo(eq(1L), any(), any(), any()))
                .thenReturn(List.of(consultaExistente(98L, horario)));
        var ex = assertThrows(NegocioException.class,
                () -> service.agendar(new ConsultaRequest(1L, 2L, horario, null)));
        assertTrue(ex.getMessage().contains("paciente"));
    }

    @Test
    void rejeitaProfissionalInativo() {
        profissional.setAtivo(false);
        assertThrows(NegocioException.class,
                () -> service.agendar(new ConsultaRequest(1L, 2L, proximoDiaUtil(9, 0), null)));
    }

    @Test
    void consultaInexistenteVoltaVazio() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertTrue(service.alterarStatus(42L, StatusConsulta.CONFIRMADA).isEmpty());
        assertFalse(service.excluir(42L));
    }

    @Test
    void reagendarIgnoraAPropriaConsulta() {
        LocalDateTime horario = proximoDiaUtil(11, 0);
        Consulta existente = consultaExistente(5L, horario);
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.buscarDoProfissionalNoPeriodo(eq(2L), any(), any(), any())).thenReturn(List.of(existente));
        assertDoesNotThrow(() -> service.reagendar(5L, new ConsultaRequest(1L, 2L, horario, "mesmo horário")));
    }

    @Test
    void naoAlteraStatusDeConsultaCancelada() {
        Consulta c = consultaExistente(7L, proximoDiaUtil(9, 0));
        c.setStatus(StatusConsulta.CANCELADA);
        when(repository.findById(7L)).thenReturn(Optional.of(c));
        assertThrows(NegocioException.class, () -> service.alterarStatus(7L, StatusConsulta.CONFIRMADA));
    }

    @Test
    void naoMarcaConsultaFuturaComoRealizada() {
        Consulta c = consultaExistente(8L, proximoDiaUtil(9, 0));
        when(repository.findById(8L)).thenReturn(Optional.of(c));
        assertThrows(NegocioException.class, () -> service.alterarStatus(8L, StatusConsulta.REALIZADA));
    }

    @Test
    void agendaMarcaHorarioOcupadoComoIndisponivel() {
        LocalDateTime horario = proximoDiaUtil(14, 30);
        when(repository.buscarDoProfissionalNoPeriodo(eq(2L), any(), any(), any()))
                .thenReturn(List.of(consultaExistente(1L, horario)));
        List<HorarioDisponivel> grade = service.agenda(2L, horario.toLocalDate());
        assertEquals(16, grade.size());
        assertFalse(grade.stream().filter(h -> h.dataHora().equals(horario)).findFirst().orElseThrow().disponivel());
        assertTrue(grade.stream().filter(h -> h.dataHora().equals(horario.plusMinutes(30))).findFirst().orElseThrow().disponivel());
    }

    @Test
    void fimDeSemanaNaoTemHorarios() {
        LocalDate sabado = LocalDate.now().plusDays(1);
        while (sabado.getDayOfWeek() != DayOfWeek.SATURDAY) {
            sabado = sabado.plusDays(1);
        }
        assertTrue(service.agenda(2L, sabado).isEmpty());
    }
}
