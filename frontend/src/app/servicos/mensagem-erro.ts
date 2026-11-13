import { HttpErrorResponse } from '@angular/common/http';

// Pega o erro que voltou da API e transforma num texto pra mostrar no alert da tela.
// O backend sempre manda { mensagem, campos } (ver GlobalExceptionHandler no Java),
// assim aparece a mensagem certa (ex: "Já existe um paciente com este CPF")
export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof HttpErrorResponse) {
    // status 0 = nem chegou no servidor, quase sempre é o backend desligado
    if (erro.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique se a API está em execução.';
    }
    if (erro.status === 404) {
      return 'Registro não encontrado.';
    }
    const corpo = erro.error as { mensagem?: string; campos?: Record<string, string> } | null;
    // erro de validacao: junta a mensagem de cada campo
    if (corpo?.campos) {
      return Object.values(corpo.campos).join(' | ');
    }
    if (corpo?.mensagem) {
      return corpo.mensagem;
    }
  }
  return 'Ocorreu um erro inesperado.';
}
