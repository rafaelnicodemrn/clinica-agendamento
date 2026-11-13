import { HttpErrorResponse } from '@angular/common/http';

// Pega o erro que veio da API e transforma num texto pra mostrar no alert da tela.
// O backend sempre manda { mensagem, campos } (ver GlobalExceptionHandler no Java)
export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof HttpErrorResponse) {
    // status 0 = nem chegou no servidor (backend desligado)
    if (erro.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique se a API está em execução.';
    }
    const corpo = erro.error as { mensagem?: string; campos?: Record<string, string> } | null;
    // erro de validação: junta a mensagem de cada campo
    if (corpo?.campos) {
      return Object.values(corpo.campos).join(' | ');
    }
    if (corpo?.mensagem) {
      return corpo.mensagem;
    }
  }
  return 'Ocorreu um erro inesperado.';
}
