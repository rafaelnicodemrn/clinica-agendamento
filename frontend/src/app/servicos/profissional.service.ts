import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Profissional } from '../entidades/Profissional';

// Chamadas pra API de profissionais
@Injectable({ providedIn: 'root' })
export class ProfissionalService {
  private apiUrl = 'http://localhost:8080/api/profissionais';

  constructor(private http: HttpClient) {}

  // sem especialidade traz todos
  listar(especialidade = ''): Observable<Profissional[]> {
    let params = new HttpParams();
    if (especialidade) {
      params = params.set('especialidade', especialidade);
    }
    return this.http.get<Profissional[]>(this.apiUrl, { params });
  }

  // so os nomes das especialidades, pros selects de filtro
  listarEspecialidades(): Observable<string[]> {
    return this.http.get<string[]>(this.apiUrl + '/especialidades');
  }

  buscar(id: number): Observable<Profissional> {
    return this.http.get<Profissional>(this.apiUrl + '/' + id);
  }

  salvar(profissional: Profissional): Observable<Profissional> {
    return this.http.post<Profissional>(this.apiUrl, profissional);
  }

  atualizar(id: number, profissional: Profissional): Observable<Profissional> {
    return this.http.put<Profissional>(this.apiUrl + '/' + id, profissional);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + id);
  }
}
