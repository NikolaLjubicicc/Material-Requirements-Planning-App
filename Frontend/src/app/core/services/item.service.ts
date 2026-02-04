import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Item, ItemCategory, ItemCreateRequest, ItemUpdateRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ItemService {
  private apiUrl = `${environment.apiUrl}/items`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Item[]> {
    return this.http.get<Item[]>(this.apiUrl);
  }

  getById(id: number): Observable<Item> {
    return this.http.get<Item>(`${this.apiUrl}/${id}`);
  }

  getBySku(sku: string): Observable<Item> {
    return this.http.get<Item>(`${this.apiUrl}/sku/${sku}`);
  }

  getByCategory(category: ItemCategory): Observable<Item[]> {
    return this.http.get<Item[]>(`${this.apiUrl}/category/${category}`);
  }

  search(name: string): Observable<Item[]> {
    return this.http.get<Item[]>(`${this.apiUrl}/search`, {
      params: { name }
    });
  }

  create(item: ItemCreateRequest): Observable<Item> {
    return this.http.post<Item>(this.apiUrl, item);
  }

  update(id: number, item: ItemUpdateRequest): Observable<Item> {
    return this.http.put<Item>(`${this.apiUrl}/${id}`, item);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
