import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Paciente } from '../entidades/Paciente';

// Service que conversa com o PacienteController do Spring.
// Os componentes nao precisam saber a URL, so chamam os metodos daqui
@Injectable({ providedIn: 'root' })
export class PacienteService {
  private apiUrl = 'http://localhost:8080/api/pacientes';

  constructor(private http: HttpClient) {}

  // GET /api/pacientes (se tiver busca manda ?nome=...)
  listar(nome = ''): Observable<Paciente[]> {
    let params = new HttpParams();
    if (nome.trim()) {
      params = params.set('nome', nome.trim());
    }
    return this.http.get<Paciente[]>(this.apiUrl, { params });
  }

  buscar(id: number): Observable<Paciente> {
    return this.http.get<Paciente>(this.apiUrl + '/' + id);
  }

  salvar(paciente: Paciente): Observable<Paciente> {
    return this.http.post<Paciente>(this.apiUrl, paciente);
  }

  atualizar(id: number, paciente: Paciente): Observable<Paciente> {
    return this.http.put<Paciente>(this.apiUrl + '/' + id, paciente);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + id);
  }
}
