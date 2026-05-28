import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Page } from '../../../shared/models/api-response.model';
import { AdminProperty, AdminPropertyDetail } from '../models/admin-property.model';

@Injectable({ providedIn: 'root' })
export class AdminPropertyService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin/properties`;

  list(page = 0, size = 10, city?: string, hostId?: number, isActive?: boolean | 'ALL', keyword?: string): Observable<Page<AdminProperty>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (city && city.trim()) params = params.set('city', city.trim());
    if (hostId !== undefined && hostId !== null) params = params.set('hostId', hostId);
    if (isActive !== undefined && isActive !== 'ALL') params = params.set('isActive', String(isActive));
    if (keyword && keyword.trim()) params = params.set('keyword', keyword.trim());

    return this.http.get<ApiResponse<Page<AdminProperty>>>(this.apiUrl, { params })
      .pipe(map(res => res.data));
  }

  getDetail(id: number): Observable<AdminPropertyDetail> {
    return this.http.get<ApiResponse<AdminPropertyDetail>>(`${this.apiUrl}/${id}`)
      .pipe(map(res => res.data));
  }

  toggleActive(id: number): Observable<AdminProperty> {
    return this.http.patch<ApiResponse<AdminProperty>>(`${this.apiUrl}/${id}/toggle-active`, {})
      .pipe(map(res => res.data));
  }

  /** Soft delete - set isActive=false. */
  softDelete(id: number): Observable<string> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${id}`)
      .pipe(map(res => res.data));
  }
}
