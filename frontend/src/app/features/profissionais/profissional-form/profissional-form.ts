import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ProfissionalService } from '../../../core/services/profissional.service';
import { mensagemDeErro } from '../../../core/util/erro';

// Formulario de profissional (novo / editar), mesma ideia do formulario de paciente
@Component({
  selector: 'app-profissional-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profissional-form.html',
})
export class ProfissionalForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProfissionalService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  private id: number | null = null;
  readonly erro = signal('');
  readonly salvando = signal(false);
  // especialidades que ja existem, aparecem como sugestão no campo (datalist)
  readonly especialidades = signal<string[]>([]);

  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    especialidade: ['', [Validators.required, Validators.maxLength(80)]],
    registro: ['', [Validators.required, Validators.maxLength(20)]],
    email: ['', [Validators.required, Validators.email]],
    ativo: [true],
  });

  get editando(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    this.service.especialidades().subscribe({ next: (l) => this.especialidades.set(l) });
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.service.buscar(this.id).subscribe({
        next: (p) => this.form.patchValue(p),
        error: (e) => this.erro.set(mensagemDeErro(e)),
      });
    }
  }

  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.salvando.set(true);
    const dados = this.form.getRawValue();
    dados.nome = dados.nome.trim();
    dados.especialidade = dados.especialidade.trim(); // evita "Cardiologia " com espaço virar outra especialidade
    const req = this.id === null ? this.service.criar(dados) : this.service.atualizar(this.id, dados);
    req.subscribe({
      next: () => this.router.navigate(['/profissionais']),
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.salvando.set(false);
      },
    });
  }
}
