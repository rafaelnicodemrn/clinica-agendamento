import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Profissional } from '../../entidades/Profissional';
import { ProfissionalService } from '../../servicos/profissional.service';
import { mensagemDeErro } from '../../servicos/mensagem-erro';

// Formulario de profissional (novo / editar), mesma ideia do formulario de paciente
@Component({
  selector: 'app-profissional-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profissional-form.html',
})
export class ProfissionalForm implements OnInit {
  private fb = inject(FormBuilder);
  private profissionalService = inject(ProfissionalService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id?: number;
  mensagemErro = '';
  salvando = false;
  especialidades: string[] = []; // aparecem como sugestao no campo (datalist)

  formProfissional = this.fb.group({
    nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    especialidade: ['', [Validators.required, Validators.maxLength(80)]],
    registro: ['', [Validators.required, Validators.maxLength(20)]],
    email: ['', [Validators.required, Validators.email]],
    ativo: [true],
  });

  // atalhos pro template
  get nome() {
    return this.formProfissional.controls.nome;
  }
  get especialidade() {
    return this.formProfissional.controls.especialidade;
  }
  get registro() {
    return this.formProfissional.controls.registro;
  }
  get email() {
    return this.formProfissional.controls.email;
  }

  ngOnInit(): void {
    this.profissionalService.listarEspecialidades().subscribe({
      next: (dados) => (this.especialidades = dados),
    });

    const idDaRota = this.route.snapshot.paramMap.get('id');
    if (idDaRota) {
      this.id = Number(idDaRota);
      this.profissionalService.buscar(this.id).subscribe({
        next: (profissional) => this.formProfissional.patchValue(profissional),
        error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
      });
    }
  }

  salvar(): void {
    if (this.formProfissional.invalid) {
      this.formProfissional.markAllAsTouched();
      return;
    }

    const profissional: Profissional = {
      nome: this.formProfissional.value.nome!.trim(),
      // trim pra "Cardiologia " com espaco nao virar outra especialidade
      especialidade: this.formProfissional.value.especialidade!.trim(),
      registro: this.formProfissional.value.registro!.trim(),
      email: this.formProfissional.value.email!.trim(),
      ativo: Boolean(this.formProfissional.value.ativo),
    };

    this.salvando = true;
    const requisicao = this.id === undefined
      ? this.profissionalService.salvar(profissional)
      : this.profissionalService.atualizar(this.id, profissional);

    requisicao.subscribe({
      next: () => this.router.navigate(['/profissionais']),
      error: (erro) => {
        this.mensagemErro = mensagemDeErro(erro); // ex: CRM ja cadastrado
        this.salvando = false;
      },
    });
  }
}
