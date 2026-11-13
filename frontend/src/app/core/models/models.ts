// Interfaces com o mesmo formato do JSON que o Spring devolve.
// Servem só pro TypeScript saber os campos (autocompletar e checar tipo)

export interface Paciente {
  id?: number; // opcional porque quando ta criando ainda nao tem id
  nome: string;
  cpf: string;
  dataNascimento: string; // vem como texto 'yyyy-MM-dd'
  telefone: string;
  email: string;
}

export interface Profissional {
  id?: number;
  nome: string;
  especialidade: string;
  registro: string;
  email: string;
  ativo: boolean;
}

// mesmos valores do enum StatusConsulta do Java
export type StatusConsulta = 'AGENDADA' | 'CONFIRMADA' | 'REALIZADA' | 'CANCELADA';

export interface Consulta {
  id: number;
  paciente: Paciente;
  profissional: Profissional;
  dataHora: string; // 'yyyy-MM-ddTHH:mm:ss'
  duracaoMinutos: number;
  status: StatusConsulta;
  observacoes?: string;
}

// o que mandamos pra marcar/remarcar (igual ao ConsultaRequest do backend)
export interface ConsultaRequest {
  pacienteId: number;
  profissionalId: number;
  dataHora: string;
  observacoes?: string;
}

// cada botão de horario da tela de agenda
export interface HorarioDisponivel {
  dataHora: string;
  disponivel: boolean;
}
