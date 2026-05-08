import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Page } from '../../../shared/models/api-response.model';
import { Review } from '../models/review.model';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/host/reviews`;

  getReviews(page = 0, size = 10): Observable<Page<Review>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<Page<Review>>>(this.apiUrl, { params }).pipe(
      map(res => res.data)
    );
  }
}
