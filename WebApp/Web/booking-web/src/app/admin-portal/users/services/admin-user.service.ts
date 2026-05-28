import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Page } from '../../../shared/models/api-response.model';
import {
  AdminUser,
  AdminUserCreateRequest,
  AdminUserDetail,
  AdminUserStats,
  AdminUserUpdateRequest,
  UserRole
} from '../models/admin-user.model';

@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin/users`;

  list(page = 0, size = 10, role?: UserRole | 'ALL', isActive?: boolean | 'ALL', keyword?: string): Observable<Page<AdminUser>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (role && role !== 'ALL') params = params.set('role', role);
    if (isActive !== undefined && isActive !== 'ALL') params = params.set('isActive', String(isActive));
    if (keyword && keyword.trim().length > 0) params = params.set('keyword', keyword.trim());

    return this.http.get<ApiResponse<Page<AdminUser>>>(this.apiUrl, { params })
      .pipe(map(res => res.data));
  }

  getDetail(id: number): Observable<AdminUserDetail> {
    return this.http.get<ApiResponse<AdminUserDetail>>(`${this.apiUrl}/${id}`)
      .pipe(map(res => res.data));
  }

  lock(id: number): Observable<AdminUser> {
    return this.http.patch<ApiResponse<AdminUser>>(`${this.apiUrl}/${id}/lock`, {})
      .pipe(map(res => res.data));
  }

  unlock(id: number): Observable<AdminUser> {
    return this.http.patch<ApiResponse<AdminUser>>(`${this.apiUrl}/${id}/unlock`, {})
      .pipe(map(res => res.data));
  }

  stats(): Observable<AdminUserStats> {
    return this.http.get<ApiResponse<AdminUserStats>>(`${this.apiUrl}/stats`)
      .pipe(map(res => res.data));
  }

  create(body: AdminUserCreateRequest): Observable<AdminUser> {
    return this.http.post<ApiResponse<AdminUser>>(this.apiUrl, body)
      .pipe(map(res => res.data));
  }

  update(id: number, body: AdminUserUpdateRequest): Observable<AdminUser> {
    return this.http.put<ApiResponse<AdminUser>>(`${this.apiUrl}/${id}`, body)
      .pipe(map(res => res.data));
  }

  delete(id: number): Observable<string> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${id}`)
      .pipe(map(res => res.data));
  }
}
