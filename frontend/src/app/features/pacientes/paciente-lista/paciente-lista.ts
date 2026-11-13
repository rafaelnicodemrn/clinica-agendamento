import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Paciente } from '../../../core/models/models';
import { PacienteService } from '../../../core/services/paciente.service';
import { mensagemDeErro } from '../../../core/util/erro';

// Tela de listagem de pacientes (com busca por nome)
@Component({
  selector: 'app-paciente-lista',
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './paciente-lista.html',
})
export class PacienteLista implements OnInit {
  private readonly service = inject(PacienteService);

  // signal = variavel "reativa", quando muda a tela atualiza sozinha
  readonly pacientes = signal<Paciente[]>([]);
  readonly erro = signal('');
  readonly carregando = signal(false);
  busca = ''; // ligado no input de busca com [(ngModel)]

  // roda quando a tela abre
  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.service.listar(this.busca).subscribe({
      next: (lista) => {
        this.pacientes.set(lista);
        this.erro.set('');
        this.carregando.set(false);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }

  excluir(p: Paciente): void {
    if (!confirm(`Excluir o paciente ${p.nome}?`)) {
      return;
    }
    // se ele tiver consulta o backend devolve 409 e a mensagem aparece no alert
    this.service.excluir(p.id!).subscribe({
      next: () => this.carregar(),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
  }

  // 12345678901 -> 123.456.789-01 (só pra exibir, no banco fica sem pontos)
  formatarCpf(cpf: string): string {
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }
}
