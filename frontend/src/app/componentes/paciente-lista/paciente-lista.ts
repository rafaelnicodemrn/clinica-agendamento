import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Paciente } from '../../entidades/Paciente';
import { PacienteService } from '../../servicos/paciente.service';
import { mensagemDeErro } from '../../servicos/mensagem-erro';

// Tela 1 - lista de pacientes com busca por nome
@Component({
  selector: 'app-paciente-lista',
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './paciente-lista.html',
})
export class PacienteLista implements OnInit {
  pacientes: Paciente[] = [];
  carregando = false;
  mensagemErro = '';
  busca = ''; // ligado no campo de busca com [(ngModel)]

  constructor(private pacienteService: PacienteService) {}

  ngOnInit(): void {
    this.carregarPacientes();
  }

  carregarPacientes(): void {
    this.carregando = true;
    this.mensagemErro = '';
    this.pacienteService.listar(this.busca).subscribe({
      next: (dados) => {
        this.pacientes = dados;
        this.carregando = false;
      },
      error: (erro) => {
        this.mensagemErro = mensagemDeErro(erro);
        this.carregando = false;
      },
    });
  }

  excluir(paciente: Paciente): void {
    if (paciente.id === undefined || !confirm('Excluir o paciente ' + paciente.nome + '?')) {
      return;
    }
    // se ele ja tiver consulta o backend devolve 409 e a mensagem aparece no alert
    this.pacienteService.excluir(paciente.id).subscribe({
      next: () => this.carregarPacientes(),
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
  }

  // 12345678901 -> 123.456.789-01 (so pra mostrar, no banco fica sem ponto)
  formatarCpf(cpf: string): string {
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }
}
