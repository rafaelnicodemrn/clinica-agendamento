import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { HorarioDisponivel, Paciente, Profissional } from '../../core/models/models';
import { ConsultaService } from '../../core/services/consulta.service';
import { PacienteService } from '../../core/services/paciente.service';
import { ProfissionalService } from '../../core/services/profissional.service';
import { mensagemDeErro } from '../../core/util/erro';

// data de amanhã no formato yyyy-MM-dd (o input type="date" usa esse formato)
function amanha(): string {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

// Tela 3 - Agenda. É aqui que o fluxo se junta:
// escolhe o paciente (etapa 1) + o profissional/especialidade (etapa 2)
// + o dia, aparece a grade de horarios e marca a consulta.
// A mesma tela é usada pra remarcar (quando vem ?consultaId= na URL)
@Component({
  selector: 'app-agenda',
  imports: [FormsModule, RouterLink],
  templateUrl: './agenda.html',
})
export class Agenda implements OnInit {
  private readonly pacienteService = inject(PacienteService);
  private readonly profissionalService = inject(ProfissionalService);
  private readonly consultaService = inject(ConsultaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // dados que vem da API
  readonly pacientes = signal<Paciente[]>([]);
  readonly profissionais = signal<Profissional[]>([]);
  readonly especialidades = signal<string[]>([]);
  readonly horarios = signal<HorarioDisponivel[]>([]);
  readonly erro = signal('');
  readonly salvando = signal(false);

  // o que o usuario escolheu na tela (ligado com [(ngModel)])
  especialidade = '';
  profissionalId: number | null = null;
  pacienteId: number | null = null;
  data = amanha();
  readonly minimo = amanha(); // não deixa escolher data passada no calendario
  horarioSelecionado = '';
  observacoes = '';
  consultaId: number | null = null; // só tem valor quando é remarcação

  // profissionais que aparecem no select: só os ativos e da especialidade escolhida.
  // (getter pra recalcular toda vez que a especialidade muda)
  get profissionaisFiltrados(): Profissional[] {
    return this.profissionais().filter(
      (p) => p.ativo && (!this.especialidade || p.especialidade === this.especialidade),
    );
  }

  // profissional selecionado, pra mostrar o nome no titulo da agenda
  get profissionalAtual(): Profissional | undefined {
    return this.profissionais().find((p) => p.id === this.profissionalId);
  }

  ngOnInit(): void {
    // parametros da URL. Outras telas mandam pra cá já com algo escolhido:
    // ?pacienteId=  (botão Agendar na lista de pacientes)
    // ?profissionalId=  (botão Ver agenda no card do profissional)
    // ?consultaId=  (botão Remarcar na lista de consultas)
    const q = this.route.snapshot.queryParamMap;

    this.pacienteService.listar().subscribe({
      next: (l) => this.pacientes.set(l),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
    this.profissionalService.especialidades().subscribe({ next: (l) => this.especialidades.set(l) });
    this.profissionalService.listar().subscribe({
      next: (l) => {
        this.profissionais.set(l);
        if (q.get('profissionalId')) {
          this.profissionalId = Number(q.get('profissionalId'));
          this.carregarAgenda();
        }
      },
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });

    if (q.get('pacienteId')) {
      this.pacienteId = Number(q.get('pacienteId'));
    }

    // remarcação: busca a consulta e preenche a tela com os dados dela
    if (q.get('consultaId')) {
      this.consultaId = Number(q.get('consultaId'));
      this.consultaService.buscar(this.consultaId).subscribe({
        next: (c) => {
          this.pacienteId = c.paciente.id!;
          this.profissionalId = c.profissional.id!;
          this.observacoes = c.observacoes ?? '';
          const dia = c.dataHora.substring(0, 10);
          this.data = dia < this.minimo ? this.minimo : dia;
          this.carregarAgenda();
        },
        error: (e) => this.erro.set(mensagemDeErro(e)),
      });
    }
  }

  // se trocou a especialidade e o profissional escolhido não é dela, limpa
  mudouEspecialidade(): void {
    if (this.profissionalId && !this.profissionaisFiltrados.some((p) => p.id === this.profissionalId)) {
      this.profissionalId = null;
      this.horarios.set([]);
    }
  }

  // busca a grade de horarios na API (chamado quando muda profissional ou data)
  carregarAgenda(): void {
    this.horarioSelecionado = '';
    if (!this.profissionalId || !this.data) {
      this.horarios.set([]);
      return;
    }
    this.consultaService.agenda(this.profissionalId, this.data).subscribe({
      next: (h) => {
        this.horarios.set(h);
        this.erro.set('');
      },
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
  }

  // '2026-11-23T09:00:00' -> '09:00' pro texto do botão
  hora(h: HorarioDisponivel): string {
    return h.dataHora.substring(11, 16);
  }

  selecionar(h: HorarioDisponivel): void {
    if (h.disponivel) {
      this.horarioSelecionado = h.dataHora;
    }
  }

  // botão de confirmar só habilita com paciente + profissional + horario escolhidos
  get podeConfirmar(): boolean {
    return !!this.pacienteId && !!this.profissionalId && !!this.horarioSelecionado && !this.salvando();
  }

  confirmar(): void {
    if (!this.podeConfirmar) {
      return;
    }
    this.salvando.set(true);
    this.erro.set('');
    const req = {
      pacienteId: this.pacienteId!,
      profissionalId: this.profissionalId!,
      dataHora: this.horarioSelecionado,
      observacoes: this.observacoes.trim() || undefined,
    };
    // nova consulta -> POST, remarcação -> PUT
    const chamada =
      this.consultaId === null
        ? this.consultaService.agendar(req)
        : this.consultaService.reagendar(this.consultaId, req);
    chamada.subscribe({
      next: () => this.router.navigate(['/consultas']),
      error: (e) => {
        // ex: paciente ja tem consulta nesse horario com outro medico
        this.erro.set(mensagemDeErro(e));
        this.salvando.set(false);
        this.carregarAgenda(); // atualiza a grade, alguem pode ter pego o horario
      },
    });
  }
}
