import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Page } from '../../../shared/models/api-response.model';
import { Booking, BookingStatus } from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/bookings`;

  getBookings(page = 0, size = 10, status?: BookingStatus | 'ALL'): Observable<Page<Booking>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status && status !== 'ALL') {
      params = params.set('status', status);
    }

    return this.http.get<ApiResponse<Page<Booking>>>(this.apiUrl, { params }).pipe(
      map(res => res.data)
    );
  }

  getBookingById(id: number): Observable<Booking> {
    return this.http.get<ApiResponse<Booking>>(`${this.apiUrl}/${id}`).pipe(
      map(res => res.data)
    );
  }

  confirmCheckOut(id: number): Observable<string> {
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/${id}/booking-completed`, {}).pipe(
      map(res => res.data)
    );
  }
}
