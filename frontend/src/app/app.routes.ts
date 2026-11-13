import { Routes } from '@angular/router';

// Rotas (telas) do sistema, na ordem do fluxo:
// 1. pacientes  ->  2. profissionais  ->  3. agenda (marcar)  ->  consultas (acompanhar)
// loadComponent carrega a tela só quando o usuario entra nela (lazy loading)
export const routes: Routes = [
  { path: '', redirectTo: 'agenda', pathMatch: 'full' },

  // ---- 1. Pacientes ----
  {
    path: 'pacientes',
    title: 'Pacientes',
    loadComponent: () =>
      import('./features/pacientes/paciente-lista/paciente-lista').then((m) => m.PacienteLista),
  },
  {
    path: 'pacientes/novo',
    title: 'Novo paciente',
    loadComponent: () =>
      import('./features/pacientes/paciente-form/paciente-form').then((m) => m.PacienteForm),
  },
  {
    // mesmo formulario do "novo", ele ve se tem :id na rota pra saber que é edição
    path: 'pacientes/:id/editar',
    title: 'Editar paciente',
    loadComponent: () =>
      import('./features/pacientes/paciente-form/paciente-form').then((m) => m.PacienteForm),
  },

  // ---- 2. Profissionais ----
  {
    path: 'profissionais',
    title: 'Profissionais',
    loadComponent: () =>
      import('./features/profissionais/profissional-lista/profissional-lista').then(
        (m) => m.ProfissionalLista,
      ),
  },
  {
    path: 'profissionais/novo',
    title: 'Novo profissional',
    loadComponent: () =>
      import('./features/profissionais/profissional-form/profissional-form').then(
        (m) => m.ProfissionalForm,
      ),
  },
  {
    path: 'profissionais/:id/editar',
    title: 'Editar profissional',
    loadComponent: () =>
      import('./features/profissionais/profissional-form/profissional-form').then(
        (m) => m.ProfissionalForm,
      ),
  },

  // ---- 3. Agenda e consultas ----
  {
    path: 'agenda',
    title: 'Agendar consulta',
    loadComponent: () => import('./features/agenda/agenda').then((m) => m.Agenda),
  },
  {
    path: 'consultas',
    title: 'Consultas',
    loadComponent: () => import('./features/consultas/consulta-lista').then((m) => m.ConsultaLista),
  },

  // qualquer rota que nao existe volta pra agenda
  { path: '**', redirectTo: 'agenda' },
];
