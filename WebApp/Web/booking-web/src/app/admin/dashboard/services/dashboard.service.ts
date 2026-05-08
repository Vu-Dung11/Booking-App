import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';

export interface HostDashboardStats {
  totalProperties: number;
  activeProperties: number;
  totalBookings: number;
  pendingBookings: number;
  confirmedBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  totalReviews: number;
  averageRating: number;
  totalRevenue: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/host/dashboard`;

  getStats(): Observable<HostDashboardStats> {
    return this.http.get<ApiResponse<HostDashboardStats>>(`${this.apiUrl}/stats`).pipe(
      map(res => res.data)
    );
  }
}
