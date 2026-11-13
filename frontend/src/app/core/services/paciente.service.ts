import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Paciente } from '../models/models';

// Service que conversa com o PacienteController do Spring (/api/pacientes).
// Cada metodo aqui é uma chamada HTTP e devolve um Observable,
// o componente faz o .subscribe() pra receber a resposta
@Injectable({ providedIn: 'root' })
export class PacienteService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/api/pacientes';

  // GET - se tiver texto na busca manda ?nome=...
  listar(nome = ''): Observable<Paciente[]> {
    let params = new HttpParams();
    if (nome.trim()) {
      params = params.set('nome', nome.trim());
    }
    return this.http.get<Paciente[]>(this.url, { params });
  }

  buscar(id: number): Observable<Paciente> {
    return this.http.get<Paciente>(`${this.url}/${id}`);
  }

  criar(paciente: Paciente): Observable<Paciente> {
    return this.http.post<Paciente>(this.url, paciente);
  }

  atualizar(id: number, paciente: Paciente): Observable<Paciente> {
    return this.http.put<Paciente>(`${this.url}/${id}`, paciente);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
