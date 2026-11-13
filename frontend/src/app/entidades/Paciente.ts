// Formato do paciente, igual ao JSON que vem do Spring
export interface Paciente {
  id?: number; // opcional porque no cadastro ainda nao tem id (quem gera é o banco)
  nome: string;
  cpf: string; // so numeros
  dataNascimento: string; // 'yyyy-MM-dd'
  telefone: string;
  email: string;
}
