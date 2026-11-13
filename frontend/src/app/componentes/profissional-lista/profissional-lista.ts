import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Profissional } from '../../entidades/Profissional';
import { ProfissionalService } from '../../servicos/profissional.service';
import { mensagemDeErro } from '../../servicos/mensagem-erro';

// Tela 2 - profissionais em cards, com filtro por especialidade
@Component({
  selector: 'app-profissional-lista',
  imports: [RouterLink, FormsModule],
  templateUrl: './profissional-lista.html',
})
export class ProfissionalLista implements OnInit {
  profissionais: Profissional[] = [];
  especialidades: string[] = [];
  mensagemErro = '';
  filtro = ''; // especialidade escolhida no select ('' = todas)

  constructor(private profissionalService: ProfissionalService) {}

  ngOnInit(): void {
    this.carregarEspecialidades();
    this.carregarProfissionais();
  }

  carregarEspecialidades(): void {
    this.profissionalService.listarEspecialidades().subscribe({
      next: (dados) => (this.especialidades = dados),
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
  }

  // chamado tambem toda vez que muda o select (ngModelChange no html)
  carregarProfissionais(): void {
    this.mensagemErro = '';
    this.profissionalService.listar(this.filtro).subscribe({
      next: (dados) => (this.profissionais = dados),
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
  }

  excluir(profissional: Profissional): void {
    if (profissional.id === undefined || !confirm('Excluir o profissional ' + profissional.nome + '?')) {
      return;
    }
    this.profissionalService.excluir(profissional.id).subscribe({
      next: () => {
        this.carregarEspecialidades(); // pode ter sido o ultimo de uma especialidade
        this.carregarProfissionais();
      },
      // se ja tiver consulta vem 409 pedindo pra inativar em vez de excluir
      error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
    });
  }
}
