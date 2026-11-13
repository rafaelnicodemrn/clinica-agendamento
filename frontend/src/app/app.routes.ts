import { Routes } from '@angular/router';

import { Agenda } from './componentes/agenda/agenda';
import { ConsultaLista } from './componentes/consulta-lista/consulta-lista';
import { PacienteForm } from './componentes/paciente-form/paciente-form';
import { PacienteLista } from './componentes/paciente-lista/paciente-lista';
import { ProfissionalForm } from './componentes/profissional-form/profissional-form';
import { ProfissionalLista } from './componentes/profissional-lista/profissional-lista';

// Rotas na ordem do fluxo:
// 1. pacientes -> 2. profissionais -> 3. agenda (marcar) -> consultas (acompanhar)
export const routes: Routes = [
  { path: '', redirectTo: 'agenda', pathMatch: 'full' },

  // 1. pacientes
  { path: 'pacientes', component: PacienteLista },
  { path: 'pacientes/novo', component: PacienteForm },
  // mesmo formulario do cadastro, ele ve o :id na rota e sabe que é edicao
  { path: 'pacientes/:id/editar', component: PacienteForm },

  // 2. profissionais
  { path: 'profissionais', component: ProfissionalLista },
  { path: 'profissionais/novo', component: ProfissionalForm },
  { path: 'profissionais/:id/editar', component: ProfissionalForm },

  // 3. agenda e consultas
  { path: 'agenda', component: Agenda },
  { path: 'consultas', component: ConsultaLista },

  // rota que nao existe volta pra agenda
  { path: '**', redirectTo: 'agenda' },
];
