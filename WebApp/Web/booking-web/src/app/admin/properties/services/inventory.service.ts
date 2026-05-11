import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import {
  BookingByDateItem,
  DayInventory,
  DayInventoryUpdateRequest,
  ExtendInventoryRequest,
  ExtendInventoryResult,
  HostCalendarResponse
} from '../models/inventory.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/host/properties`;

  /** Lấy calendar view của 1 property cho khoảng [from, from + days - 1]. */
  getCalendar(propertyId: number, from: string, days: number = 30): Observable<HostCalendarResponse> {
    const params = new HttpParams()
      .set('from', from)
      .set('days', String(days));
    return this.http.get<ApiResponse<HostCalendarResponse>>(`${this.apiUrl}/${propertyId}/calendar`, { params })
      .pipe(map(res => res.data));
  }

  /** Bulk update availableCount cho [fromDate, toDate] của 1 room. */
  bulkUpdate(propertyId: number, roomId: number, body: DayInventoryUpdateRequest): Observable<DayInventory[]> {
    return this.http.patch<ApiResponse<DayInventory[]>>(
      `${this.apiUrl}/${propertyId}/rooms/${roomId}/inventory`,
      body
    ).pipe(map(res => res.data));
  }

  /** Mở thêm inventory đến untilDate. */
  extend(propertyId: number, roomId: number, untilDate: string): Observable<ExtendInventoryResult> {
    const body: ExtendInventoryRequest = { untilDate };
    return this.http.post<ApiResponse<ExtendInventoryResult>>(
      `${this.apiUrl}/${propertyId}/rooms/${roomId}/inventory/extend`,
      body
    ).pipe(map(res => res.data));
  }

  /** Drill-down: lấy list booking đang giữ chỗ ngày `date` của 1 room. */
  getBookingsForDay(propertyId: number, roomId: number, date: string): Observable<BookingByDateItem[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<ApiResponse<BookingByDateItem[]>>(
      `${this.apiUrl}/${propertyId}/rooms/${roomId}/bookings/by-date`,
      { params }
    ).pipe(map(res => res.data));
  }
}
