import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Inventory, InventoryCreateRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private apiUrl = `${environment.apiUrl}/inventory`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Inventory[]> {
    return this.http.get<Inventory[]>(this.apiUrl);
  }

  getById(id: number): Observable<Inventory> {
    return this.http.get<Inventory>(`${this.apiUrl}/${id}`);
  }

  getByItemId(itemId: number): Observable<Inventory> {
    return this.http.get<Inventory>(`${this.apiUrl}/item/${itemId}`);
  }

  create(inventory: InventoryCreateRequest): Observable<Inventory> {
    return this.http.post<Inventory>(this.apiUrl, inventory);
  }

  setQuantity(itemId: number, quantity: number): Observable<Inventory> {
    return this.http.put<Inventory>(
      `${this.apiUrl}/item/${itemId}/quantity`,
      null,
      { params: { quantity: quantity.toString() } }
    );
  }

  addStock(itemId: number, quantity: number): Observable<Inventory> {
    return this.http.post<Inventory>(
      `${this.apiUrl}/item/${itemId}/add`,
      null,
      { params: { quantity: quantity.toString() } }
    );
  }

  removeStock(itemId: number, quantity: number): Observable<Inventory> {
    return this.http.post<Inventory>(
      `${this.apiUrl}/item/${itemId}/remove`,
      null,
      { params: { quantity: quantity.toString() } }
    );
  }

  reserve(itemId: number, quantity: number): Observable<Inventory> {
    return this.http.post<Inventory>(
      `${this.apiUrl}/item/${itemId}/reserve`,
      null,
      { params: { quantity: quantity.toString() } }
    );
  }

  releaseReservation(itemId: number, quantity: number): Observable<Inventory> {
    return this.http.post<Inventory>(
      `${this.apiUrl}/item/${itemId}/release`,
      null,
      { params: { quantity: quantity.toString() } }
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
