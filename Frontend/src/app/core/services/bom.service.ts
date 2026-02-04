import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BomItem, BomItemCreateRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class BomService {
  private apiUrl = `${environment.apiUrl}/bom`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<BomItem[]> {
    return this.http.get<BomItem[]>(this.apiUrl);
  }

  getById(id: number): Observable<BomItem> {
    return this.http.get<BomItem>(`${this.apiUrl}/${id}`);
  }

  getByParentId(parentId: number): Observable<BomItem[]> {
    return this.http.get<BomItem[]>(`${this.apiUrl}/parent/${parentId}`);
  }

  getByComponentId(componentId: number): Observable<BomItem[]> {
    return this.http.get<BomItem[]>(`${this.apiUrl}/component/${componentId}`);
  }

  create(bom: BomItemCreateRequest): Observable<BomItem> {
    return this.http.post<BomItem>(this.apiUrl, bom);
  }

  updateQuantity(id: number, quantity: number): Observable<BomItem> {
    return this.http.put<BomItem>(`${this.apiUrl}/${id}`, null, {
      params: { quantity: quantity.toString() }
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
