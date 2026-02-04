import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ProductionPlan,
  ProductionPlanCreateRequest,
  ProductionPlanUpdateRequest,
  PlanStatus
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class ProductionPlanService {
  private apiUrl = `${environment.apiUrl}/production-plans`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ProductionPlan[]> {
    return this.http.get<ProductionPlan[]>(this.apiUrl);
  }

  getById(id: number): Observable<ProductionPlan> {
    return this.http.get<ProductionPlan>(`${this.apiUrl}/${id}`);
  }

  getByStatus(status: PlanStatus): Observable<ProductionPlan[]> {
    return this.http.get<ProductionPlan[]>(`${this.apiUrl}/status/${status}`);
  }

  getPending(): Observable<ProductionPlan[]> {
    return this.http.get<ProductionPlan[]>(`${this.apiUrl}/pending`);
  }

  getByDateRange(startDate: string, endDate: string): Observable<ProductionPlan[]> {
    return this.http.get<ProductionPlan[]>(`${this.apiUrl}/date-range`, {
      params: { startDate, endDate }
    });
  }

  create(plan: ProductionPlanCreateRequest): Observable<ProductionPlan> {
    return this.http.post<ProductionPlan>(this.apiUrl, plan);
  }

  update(id: number, plan: ProductionPlanUpdateRequest): Observable<ProductionPlan> {
    return this.http.put<ProductionPlan>(`${this.apiUrl}/${id}`, plan);
  }

  updateStatus(id: number, status: PlanStatus): Observable<ProductionPlan> {
    return this.http.patch<ProductionPlan>(
      `${this.apiUrl}/${id}/status`,
      null,
      { params: { status } }
    );
  }

  cancel(id: number): Observable<ProductionPlan> {
    return this.http.post<ProductionPlan>(`${this.apiUrl}/${id}/cancel`, null);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
