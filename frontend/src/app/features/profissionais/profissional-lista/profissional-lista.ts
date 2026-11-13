import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Profissional } from '../../../core/models/models';
import { ProfissionalService } from '../../../core/services/profissional.service';
import { mensagemDeErro } from '../../../core/util/erro';

// Tela 2: profissionais em cards, com filtro por especialidade
@Component({
  selector: 'app-profissional-lista',
  imports: [RouterLink, FormsModule],
  templateUrl: './profissional-lista.html',
})
export class ProfissionalLista implements OnInit {
  private readonly service = inject(ProfissionalService);

  readonly profissionais = signal<Profissional[]>([]);
  readonly especialidades = signal<string[]>([]);
  readonly erro = signal('');
  filtro = ''; // especialidade escolhida no select ('' = todas)

  ngOnInit(): void {
    this.carregar();
  }

  // carrega as especialidades (pro select) e a lista
  carregar(): void {
    this.service.especialidades().subscribe({
      next: (lista) => this.especialidades.set(lista),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
    this.filtrar();
  }

  // chamado toda vez que muda o select (ngModelChange no html)
  filtrar(): void {
    this.service.listar(this.filtro).subscribe({
      next: (lista) => {
        this.profissionais.set(lista);
        this.erro.set('');
      },
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
  }

  excluir(p: Profissional): void {
    if (!confirm(`Excluir o profissional ${p.nome}?`)) {
      return;
    }
    this.service.excluir(p.id!).subscribe({
      next: () => this.carregar(),
      error: (e) => this.erro.set(mensagemDeErro(e)), // ex: ja tem consulta, tem que inativar
    });
  }
}
