import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MrpResult, PlannedOrder, OrderStatus, StatusUpdateResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class MrpService {
  private apiUrl = `${environment.apiUrl}/mrp`;

  constructor(private http: HttpClient) {}

  runMrp(planId: number): Observable<MrpResult> {
    return this.http.post<MrpResult>(`${this.apiUrl}/run/${planId}`, null);
  }

  getPurchaseOrders(): Observable<PlannedOrder[]> {
    return this.http.get<PlannedOrder[]>(`${this.apiUrl}/orders/purchase`);
  }

  getProductionOrders(): Observable<PlannedOrder[]> {
    return this.http.get<PlannedOrder[]>(`${this.apiUrl}/orders/production`);
  }

  getOrdersByPlan(planId: number): Observable<PlannedOrder[]> {
    return this.http.get<PlannedOrder[]>(`${this.apiUrl}/orders/plan/${planId}`);
  }

  getOrdersByDateRange(startDate: string, endDate: string): Observable<PlannedOrder[]> {
    return this.http.get<PlannedOrder[]>(`${this.apiUrl}/orders/date-range`, {
      params: { startDate, endDate }
    });
  }

  getOrderById(orderId: number): Observable<PlannedOrder> {
    return this.http.get<PlannedOrder>(`${this.apiUrl}/orders/${orderId}`);
  }

  updateOrderStatus(orderId: number, status: OrderStatus): Observable<StatusUpdateResponse> {
    return this.http.put<StatusUpdateResponse>(`${this.apiUrl}/orders/${orderId}/status`, { status });
  }
}
