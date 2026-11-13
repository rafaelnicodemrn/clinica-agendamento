import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Consulta, StatusConsulta } from '../../entidades/Consulta';
import { Profissional } from '../../entidades/Profissional';
import { ConsultaService } from '../../servicos/consulta.service';
import { ProfissionalService } from '../../servicos/profissional.service';
import { mensagemDeErro } from '../../servicos/mensagem-erro';

// Lista das consultas marcadas. Daqui da pra confirmar, marcar como realizada,
// remarcar, cancelar e excluir
@Component({
  selector: 'app-consulta-lista',
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './consulta-lista.html',
})
export class ConsultaLista implements OnInit {
  consultas: Consulta[] = [];
  profissionais: Profissional[] = []; // pro select de filtro
  mensagemErro = '';
  listaStatus: StatusConsulta[] = ['AGENDADA', 'CONFIRMADA', 'REALIZADA', 'CANCELADA'];

  // filtros da tela
  filtroProfissional: number | null = null;
  filtroStatus = '';

  constructor(
    private consultaService: ConsultaService,
    private profissionalService: ProfissionalService,
  ) {}

  ngOnInit(): void {
    this.profissionalService.listar().subscribe({
      next: (dados) => (this.profissionais = dados),
    });
    this.carregarConsultas();
  }

  carregarConsultas(): void {
    this.mensagemErro = '';
    this.consultaService.listar(this.filtroProfissional ?? undefined, this.filtroStatus).subscribe({
      next: (dados) => (this.consultas = dados),
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
  }

  // os botoes de acao so aparecem pra consulta que ainda vai acontecer
  estaAtiva(consulta: Consulta): boolean {
    return consulta.status === 'AGENDADA' || consulta.status === 'CONFIRMADA';
  }

  // se o backend recusar (ex: marcar como realizada uma consulta futura)
  // a mensagem dele aparece no alert
  mudarStatus(consulta: Consulta, status: StatusConsulta): void {
    this.consultaService.alterarStatus(consulta.id, status).subscribe({
      next: () => this.carregarConsultas(),
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
  }

  cancelar(consulta: Consulta): void {
    if (confirm('Cancelar a consulta de ' + consulta.paciente.nome + '?')) {
      this.mudarStatus(consulta, 'CANCELADA');
    }
  }

  excluir(consulta: Consulta): void {
    if (!confirm('Excluir de vez este registro de consulta?')) {
      return;
    }
    this.consultaService.excluir(consulta.id).subscribe({
      next: () => this.carregarConsultas(),
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
  }

  // cor do badge de cada status (classes do bootstrap)
  corDoStatus(status: StatusConsulta): string {
    switch (status) {
      case 'AGENDADA':
        return 'text-bg-primary';
      case 'CONFIRMADA':
        return 'text-bg-info';
      case 'REALIZADA':
        return 'text-bg-success';
      default:
        return 'text-bg-secondary';
    }
  }
}
