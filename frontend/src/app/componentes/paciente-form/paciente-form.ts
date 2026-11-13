import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Paciente } from '../../entidades/Paciente';
import { PacienteService } from '../../servicos/paciente.service';
import { mensagemDeErro } from '../../servicos/mensagem-erro';

// data de hoje no formato do input date (yyyy-MM-dd), no horario local
function hojeFormatado(): string {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return d.getFullYear() + '-' + mes + '-' + dia;
}

// Formulario de paciente - serve pro cadastro e pra edicao.
// Se a rota tiver :id é edicao, senao é um paciente novo
@Component({
  selector: 'app-paciente-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './paciente-form.html',
})
export class PacienteForm implements OnInit {
  private fb = inject(FormBuilder);
  private pacienteService = inject(PacienteService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id?: number; // so tem valor na edicao
  mensagemErro = '';
  salvando = false;
  hoje = hojeFormatado(); // usado no [max] do campo de data

  // mesmas regras que o backend confere. La é a validacao "de verdade",
  // aqui é pra avisar o usuario antes de mandar
  formPaciente = this.fb.group({
    nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    cpf: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
    dataNascimento: ['', Validators.required],
    telefone: ['', [Validators.required, Validators.pattern(/^\d{10,11}$/)]],
    email: ['', [Validators.required, Validators.email]],
  });

  // atalhos pro template nao ficar cheio de formPaciente.get('...')
  get nome() {
    return this.formPaciente.controls.nome;
  }
  get cpf() {
    return this.formPaciente.controls.cpf;
  }
  get dataNascimento() {
    return this.formPaciente.controls.dataNascimento;
  }
  get telefone() {
    return this.formPaciente.controls.telefone;
  }
  get email() {
    return this.formPaciente.controls.email;
  }

  ngOnInit(): void {
    // se veio id na url busca o paciente e preenche o formulario
    const idDaRota = this.route.snapshot.paramMap.get('id');
    if (idDaRota) {
      this.id = Number(idDaRota);
      this.pacienteService.buscar(this.id).subscribe({
        next: (paciente) => this.formPaciente.patchValue(paciente),
        error: (erro) => (this.mensagemErro = mensagemDeErro(erro)),
      });
    }
  }

  salvar(): void {
    if (this.formPaciente.invalid) {
      this.formPaciente.markAllAsTouched(); // mostra o erro ate dos campos que nem foram tocados
      return;
    }
    if (this.formPaciente.value.dataNascimento! >= this.hoje) {
      this.mensagemErro = 'A data de nascimento deve estar no passado.';
      return;
    }

    const paciente: Paciente = {
      nome: this.formPaciente.value.nome!.trim(),
      cpf: this.formPaciente.value.cpf!,
      dataNascimento: this.formPaciente.value.dataNascimento!,
      telefone: this.formPaciente.value.telefone!,
      email: this.formPaciente.value.email!.trim(),
    };

    this.salvando = true; // trava o botao pra nao salvar 2x
    // sem id -> POST (novo), com id -> PUT (edicao)
    const requisicao = this.id === undefined
      ? this.pacienteService.salvar(paciente)
      : this.pacienteService.atualizar(this.id, paciente);

    requisicao.subscribe({
      next: () => this.router.navigate(['/pacientes']),
      error: (erro) => {
        this.mensagemErro = mensagemDeErro(erro); // ex: cpf ja cadastrado
        this.salvando = false;
      },
    });
  }
}
