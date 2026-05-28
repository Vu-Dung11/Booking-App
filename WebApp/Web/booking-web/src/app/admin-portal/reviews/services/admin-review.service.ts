import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Page } from '../../../shared/models/api-response.model';
import { AdminReview, AdminReviewDetail } from '../models/admin-review.model';

@Injectable({ providedIn: 'root' })
export class AdminReviewService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin/reviews`;

  list(page = 0, size = 10, rating?: number | 'ALL', propertyId?: number, guestId?: number, keyword?: string): Observable<Page<AdminReview>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (rating !== undefined && rating !== 'ALL') params = params.set('rating', rating);
    if (propertyId) params = params.set('propertyId', propertyId);
    if (guestId) params = params.set('guestId', guestId);
    if (keyword && keyword.trim()) params = params.set('keyword', keyword.trim());

    return this.http.get<ApiResponse<Page<AdminReview>>>(this.apiUrl, { params })
      .pipe(map(res => res.data));
  }

  getDetail(id: number): Observable<AdminReviewDetail> {
    return this.http.get<ApiResponse<AdminReviewDetail>>(`${this.apiUrl}/${id}`)
      .pipe(map(res => res.data));
  }

  delete(id: number): Observable<string> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${id}`)
      .pipe(map(res => res.data));
  }
}
