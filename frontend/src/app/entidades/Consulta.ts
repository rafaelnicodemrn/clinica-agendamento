import { Paciente } from './Paciente';
import { Profissional } from './Profissional';

// mesmos valores do enum StatusConsulta do Java
export type StatusConsulta = 'AGENDADA' | 'CONFIRMADA' | 'REALIZADA' | 'CANCELADA';

// consulta como vem da API (ja com o paciente e o profissional completos dentro)
export interface Consulta {
  id: number;
  paciente: Paciente;
  profissional: Profissional;
  dataHora: string; // 'yyyy-MM-ddTHH:mm:ss'
  duracaoMinutos: number;
  status: StatusConsulta;
  observacoes?: string;
}

// o que a gente manda pra marcar/remarcar: so os ids e o horario
export interface ConsultaRequest {
  pacienteId: number;
  profissionalId: number;
  dataHora: string;
  observacoes?: string;
}

// cada botao de horario da tela de agenda
export interface HorarioDisponivel {
  dataHora: string;
  disponivel: boolean;
}
