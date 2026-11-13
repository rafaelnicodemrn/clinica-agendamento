import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Profissional } from '../models/models';

// Chamadas pra API de profissionais (/api/profissionais)
@Injectable({ providedIn: 'root' })
export class ProfissionalService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/api/profissionais';

  // sem especialidade traz todos
  listar(especialidade = ''): Observable<Profissional[]> {
    let params = new HttpParams();
    if (especialidade) {
      params = params.set('especialidade', especialidade);
    }
    return this.http.get<Profissional[]>(this.url, { params });
  }

  // só os nomes das especialidades, pros selects de filtro
  especialidades(): Observable<string[]> {
    return this.http.get<string[]>(`${this.url}/especialidades`);
  }

  buscar(id: number): Observable<Profissional> {
    return this.http.get<Profissional>(`${this.url}/${id}`);
  }

  criar(profissional: Profissional): Observable<Profissional> {
    return this.http.post<Profissional>(this.url, profissional);
  }

  atualizar(id: number, profissional: Profissional): Observable<Profissional> {
    return this.http.put<Profissional>(`${this.url}/${id}`, profissional);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
