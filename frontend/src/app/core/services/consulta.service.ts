import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Consulta, ConsultaRequest, HorarioDisponivel, StatusConsulta } from '../models/models';

// Chamadas pra API de consultas (/api/consultas)
@Injectable({ providedIn: 'root' })
export class ConsultaService {
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/api/consultas';

  // os filtros são opcionais, só vai na URL o que foi preenchido
  listar(filtros: { profissionalId?: number; pacienteId?: number; status?: string } = {}): Observable<Consulta[]> {
    let params = new HttpParams();
    if (filtros.profissionalId) params = params.set('profissionalId', filtros.profissionalId);
    if (filtros.pacienteId) params = params.set('pacienteId', filtros.pacienteId);
    if (filtros.status) params = params.set('status', filtros.status);
    return this.http.get<Consulta[]>(this.url, { params });
  }

  buscar(id: number): Observable<Consulta> {
    return this.http.get<Consulta>(`${this.url}/${id}`);
  }

  // horarios do dia do profissional, data no formato yyyy-MM-dd
  agenda(profissionalId: number, data: string): Observable<HorarioDisponivel[]> {
    const params = new HttpParams().set('profissionalId', profissionalId).set('data', data);
    return this.http.get<HorarioDisponivel[]>(`${this.url}/agenda`, { params });
  }

  agendar(req: ConsultaRequest): Observable<Consulta> {
    return this.http.post<Consulta>(this.url, req);
  }

  reagendar(id: number, req: ConsultaRequest): Observable<Consulta> {
    return this.http.put<Consulta>(`${this.url}/${id}`, req);
  }

  // PATCH mandando só { status: 'CONFIRMADA' } por exemplo
  alterarStatus(id: number, status: StatusConsulta): Observable<Consulta> {
    return this.http.patch<Consulta>(`${this.url}/${id}/status`, { status });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
