import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Consulta, Profissional, StatusConsulta } from '../../core/models/models';
import { ConsultaService } from '../../core/services/consulta.service';
import { ProfissionalService } from '../../core/services/profissional.service';
import { mensagemDeErro } from '../../core/util/erro';

// Lista de consultas marcadas. Daqui da pra confirmar, marcar como realizada,
// remarcar, cancelar e excluir
@Component({
  selector: 'app-consulta-lista',
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './consulta-lista.html',
})
export class ConsultaLista implements OnInit {
  private readonly service = inject(ConsultaService);
  private readonly profissionalService = inject(ProfissionalService);

  readonly consultas = signal<Consulta[]>([]);
  readonly profissionais = signal<Profissional[]>([]); // pro select de filtro
  readonly erro = signal('');
  readonly statusDisponiveis: StatusConsulta[] = ['AGENDADA', 'CONFIRMADA', 'REALIZADA', 'CANCELADA'];

  // filtros da tela
  filtroStatus = '';
  filtroProfissional: number | null = null;

  ngOnInit(): void {
    this.profissionalService.listar().subscribe({ next: (l) => this.profissionais.set(l) });
    this.carregar();
  }

  carregar(): void {
    this.service
      .listar({
        status: this.filtroStatus || undefined,
        profissionalId: this.filtroProfissional ?? undefined,
      })
      .subscribe({
        next: (l) => {
          this.consultas.set(l);
          this.erro.set('');
        },
        error: (e) => this.erro.set(mensagemDeErro(e)),
      });
  }

  // os botões de ação só aparecem pra consulta que ainda vai acontecer
  ativa(c: Consulta): boolean {
    return c.status === 'AGENDADA' || c.status === 'CONFIRMADA';
  }

  // muda o status e recarrega a lista. Se o backend recusar (ex: marcar como
  // realizada uma consulta futura) a mensagem aparece no alert
  alterar(c: Consulta, status: StatusConsulta): void {
    this.service.alterarStatus(c.id, status).subscribe({
      next: () => this.carregar(),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
  }

  cancelar(c: Consulta): void {
    if (confirm(`Cancelar a consulta de ${c.paciente.nome}?`)) {
      this.alterar(c, 'CANCELADA');
    }
  }

  excluir(c: Consulta): void {
    if (!confirm('Excluir definitivamente este registro de consulta?')) {
      return;
    }
    this.service.excluir(c.id).subscribe({
      next: () => this.carregar(),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
  }

  // cor do badge de cada status (classes do bootstrap)
  classeStatus(s: StatusConsulta): string {
    return {
      AGENDADA: 'text-bg-primary',
      CONFIRMADA: 'text-bg-info',
      REALIZADA: 'text-bg-success',
      CANCELADA: 'text-bg-secondary',
    }[s];
  }
}
