import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { PacienteService } from '../../../core/services/paciente.service';
import { mensagemDeErro } from '../../../core/util/erro';

// Formulario de paciente. Serve pra cadastrar e pra editar:
// se a rota tiver um :id é edição, se não é cadastro novo
@Component({
  selector: 'app-paciente-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './paciente-form.html',
})
export class PacienteForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PacienteService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  private id: number | null = null;
  readonly erro = signal('');
  readonly salvando = signal(false);

  // Formulario reativo com as validações do lado do front.
  // São as mesmas regras que o backend confere (lá é a validação "de verdade",
  // aqui é pra avisar o usuario antes de mandar)
  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    cpf: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
    dataNascimento: ['', [Validators.required]],
    telefone: ['', [Validators.required, Validators.pattern(/^\d{10,11}$/)]],
    email: ['', [Validators.required, Validators.email]],
  });

  // data de hoje em yyyy-MM-dd, usada no [max] do input de data
  readonly hoje = new Date().toISOString().substring(0, 10);

  get editando(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    // se veio id na URL busca o paciente e preenche o formulario
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.service.buscar(this.id).subscribe({
        next: (p) => this.form.patchValue(p),
        error: (e) => this.erro.set(mensagemDeErro(e)),
      });
    }
  }

  // usado no html pra pintar o campo de vermelho (is-invalid do bootstrap)
  // só mostra o erro depois que a pessoa mexeu no campo
  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // faz aparecer o erro em todos os campos
      return;
    }
    if (this.form.controls.dataNascimento.value >= this.hoje) {
      this.erro.set('A data de nascimento deve estar no passado.');
      return;
    }
    this.salvando.set(true); // desabilita o botão pra não clicar 2x
    const dados = this.form.getRawValue();
    dados.nome = dados.nome.trim();
    // novo -> POST, edição -> PUT
    const req = this.id === null ? this.service.criar(dados) : this.service.atualizar(this.id, dados);
    req.subscribe({
      next: () => this.router.navigate(['/pacientes']),
      error: (e) => {
        this.erro.set(mensagemDeErro(e)); // ex: CPF ja cadastrado
        this.salvando.set(false);
      },
    });
  }
}
