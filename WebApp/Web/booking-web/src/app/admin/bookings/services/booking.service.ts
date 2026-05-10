import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Page } from '../../../shared/models/api-response.model';
import { Booking, BookingStatus, BookingDetailResponse, ConfirmBookingRequest, CancelBookingRequest } from '../models/booking.model';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/host/bookings`;

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

  /** Trả về DTO chi tiết booking (kèm guest contact, payments, pendingExpiresAt). */
  getBookingDetail(id: number): Observable<BookingDetailResponse> {
    return this.http.get<ApiResponse<BookingDetailResponse>>(`${this.apiUrl}/${id}`).pipe(
      map(res => res.data)
    );
  }

  /** Backwards-compat alias. */
  getBookingById(id: number): Observable<BookingDetailResponse> {
    return this.getBookingDetail(id);
  }

  /** Host xác nhận thanh toán thủ công cho booking PENDING. */
  confirmBooking(id: number, body?: ConfirmBookingRequest): Observable<BookingDetailResponse> {
    return this.http.post<ApiResponse<BookingDetailResponse>>(
      `${this.apiUrl}/${id}/confirm`,
      body ?? {}
    ).pipe(map(res => res.data));
  }

  /** Host huỷ booking PENDING hoặc CONFIRMED. */
  cancelBooking(id: number, reason?: string): Observable<BookingDetailResponse> {
    const body: CancelBookingRequest = { reason: reason ?? '' };
    return this.http.post<ApiResponse<BookingDetailResponse>>(
      `${this.apiUrl}/${id}/cancel`,
      body
    ).pipe(map(res => res.data));
  }
}
