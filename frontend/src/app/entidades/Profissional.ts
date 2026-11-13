// Formato do profissional
export interface Profissional {
  id?: number;
  nome: string;
  especialidade: string;
  registro: string; // CRM
  email: string;
  ativo: boolean; // inativo nao aparece pra marcar consulta
}
