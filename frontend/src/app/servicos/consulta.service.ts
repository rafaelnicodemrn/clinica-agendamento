import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Consulta, ConsultaRequest, HorarioDisponivel, StatusConsulta } from '../entidades/Consulta';

// Chamadas pra API de consultas e da agenda
@Injectable({ providedIn: 'root' })
export class ConsultaService {
  private apiUrl = 'http://localhost:8080/api/consultas';

  constructor(private http: HttpClient) {}

  // os filtros sao opcionais, so vai na url o que foi escolhido na tela
  listar(profissionalId?: number, status?: string): Observable<Consulta[]> {
    let params = new HttpParams();
    if (profissionalId) {
      params = params.set('profissionalId', profissionalId);
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<Consulta[]>(this.apiUrl, { params });
  }

  buscar(id: number): Observable<Consulta> {
    return this.http.get<Consulta>(this.apiUrl + '/' + id);
  }

  // horarios do dia do profissional (data no formato yyyy-MM-dd)
  agenda(profissionalId: number, data: string): Observable<HorarioDisponivel[]> {
    const params = new HttpParams().set('profissionalId', profissionalId).set('data', data);
    return this.http.get<HorarioDisponivel[]>(this.apiUrl + '/agenda', { params });
  }

  agendar(consulta: ConsultaRequest): Observable<Consulta> {
    return this.http.post<Consulta>(this.apiUrl, consulta);
  }

  reagendar(id: number, consulta: ConsultaRequest): Observable<Consulta> {
    return this.http.put<Consulta>(this.apiUrl + '/' + id, consulta);
  }

  // PATCH mandando so o status, ex: { status: 'CONFIRMADA' }
  alterarStatus(id: number, status: StatusConsulta): Observable<Consulta> {
    return this.http.patch<Consulta>(this.apiUrl + '/' + id + '/status', { status });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + id);
  }
}
