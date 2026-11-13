import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ConsultaRequest, HorarioDisponivel } from '../../entidades/Consulta';
import { Paciente } from '../../entidades/Paciente';
import { Profissional } from '../../entidades/Profissional';
import { ConsultaService } from '../../servicos/consulta.service';
import { PacienteService } from '../../servicos/paciente.service';
import { ProfissionalService } from '../../servicos/profissional.service';
import { mensagemDeErro } from '../../servicos/mensagem-erro';

// data de amanha no formato do input date (yyyy-MM-dd)
function amanha(): string {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return d.getFullYear() + '-' + mes + '-' + dia;
}

// Tela 3 - Agenda. Aqui o fluxo todo se junta:
// escolhe o paciente (etapa 1) + especialidade/profissional (etapa 2) + o dia,
// aparece a grade de horarios e marca a consulta.
// A mesma tela serve pra remarcar, quando vem ?consultaId= na url
@Component({
  selector: 'app-agenda',
  imports: [FormsModule, RouterLink],
  templateUrl: './agenda.html',
})
export class Agenda implements OnInit {
  // dados que vem da API
  pacientes: Paciente[] = [];
  profissionais: Profissional[] = [];
  especialidades: string[] = [];
  horarios: HorarioDisponivel[] = [];
  mensagemErro = '';
  salvando = false;

  // o que o usuario escolhe na tela (ligado com [(ngModel)])
  pacienteId: number | null = null;
  especialidade = '';
  profissionalId: number | null = null;
  data = amanha();
  dataMinima = amanha(); // nao deixa escolher dia que ja passou
  horarioEscolhido = '';
  observacoes = '';
  consultaId: number | null = null; // so tem valor quando é remarcacao

  constructor(
    private pacienteService: PacienteService,
    private profissionalService: ProfissionalService,
    private consultaService: ConsultaService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  // profissionais que aparecem no select: so os ativos e da especialidade escolhida
  get profissionaisFiltrados(): Profissional[] {
    return this.profissionais.filter(
      (p) => p.ativo && (this.especialidade === '' || p.especialidade === this.especialidade),
    );
  }

  // profissional escolhido, pra mostrar o nome em cima da agenda
  get profissionalEscolhido(): Profissional | undefined {
    return this.profissionais.find((p) => p.id === this.profissionalId);
  }

  // o botao de marcar so libera com paciente, profissional e horario escolhidos
  get podeConfirmar(): boolean {
    return this.pacienteId !== null && this.profissionalId !== null && this.horarioEscolhido !== '' && !this.salvando;
  }

  ngOnInit(): void {
    // outras telas mandam pra ca ja com alguma coisa escolhida pela url:
    //   ?pacienteId=      (botao Agendar na lista de pacientes)
    //   ?profissionalId=  (botao Ver agenda no card do profissional)
    //   ?consultaId=      (botao Remarcar na lista de consultas)
    const parametros = this.route.snapshot.queryParamMap;

    this.pacienteService.listar().subscribe({
      next: (dados) => (this.pacientes = dados),
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
    this.profissionalService.listarEspecialidades().subscribe({
      next: (dados) => (this.especialidades = dados),
    });
    this.profissionalService.listar().subscribe({
      next: (dados) => {
        this.profissionais = dados;
        if (parametros.get('profissionalId')) {
          this.profissionalId = Number(parametros.get('profissionalId'));
          this.carregarHorarios();
        }
      },
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });

    if (parametros.get('pacienteId')) {
      this.pacienteId = Number(parametros.get('pacienteId'));
    }

    // remarcacao: busca a consulta e preenche a tela com os dados dela
    if (parametros.get('consultaId')) {
      this.consultaId = Number(parametros.get('consultaId'));
      this.consultaService.buscar(this.consultaId).subscribe({
        next: (consulta) => {
          this.pacienteId = consulta.paciente.id!;
          this.profissionalId = consulta.profissional.id!;
          this.observacoes = consulta.observacoes ?? '';
          const diaDaConsulta = consulta.dataHora.substring(0, 10);
          this.data = diaDaConsulta < this.dataMinima ? this.dataMinima : diaDaConsulta;
          this.carregarHorarios();
        },
        error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
      });
    }
  }

  // trocou a especialidade e o profissional escolhido nao é dela? limpa
  trocouEspecialidade(): void {
    const aindaNaLista = this.profissionaisFiltrados.some((p) => p.id === this.profissionalId);
    if (!aindaNaLista) {
      this.profissionalId = null;
      this.horarios = [];
    }
  }

  // busca a grade do dia na API (roda quando muda o profissional ou a data)
  carregarHorarios(): void {
    this.horarioEscolhido = '';
    if (this.profissionalId === null || !this.data) {
      this.horarios = [];
      return;
    }
    this.consultaService.agenda(this.profissionalId, this.data).subscribe({
      next: (dados) => {
        this.horarios = dados;
        this.mensagemErro = '';
      },
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
  }

  // '2026-11-23T09:00:00' -> '09:00', pro texto do botao
  hora(horario: HorarioDisponivel): string {
    return horario.dataHora.substring(11, 16);
  }

  escolherHorario(horario: HorarioDisponivel): void {
    if (horario.disponivel) {
      this.horarioEscolhido = horario.dataHora;
    }
  }

  confirmar(): void {
    if (!this.podeConfirmar) {
      return;
    }
    const consulta: ConsultaRequest = {
      pacienteId: Number(this.pacienteId),
      profissionalId: Number(this.profissionalId),
      dataHora: this.horarioEscolhido,
      observacoes: this.observacoes.trim() || undefined,
    };

    this.salvando = true;
    this.mensagemErro = '';
    // consulta nova -> POST, remarcacao -> PUT
    const requisicao = this.consultaId === null
      ? this.consultaService.agendar(consulta)
      : this.consultaService.reagendar(this.consultaId, consulta);

    requisicao.subscribe({
      next: () => this.router.navigate(['/consultas']),
      error: (erro) => {
        // ex: o paciente ja tem consulta nesse horario com outro medico
        this.mensagemErro = mensagemDeErro(erro);
        this.salvando = false;
        this.carregarHorarios(); // atualiza a grade, alguem pode ter pego o horario
      },
    });
  }
}
