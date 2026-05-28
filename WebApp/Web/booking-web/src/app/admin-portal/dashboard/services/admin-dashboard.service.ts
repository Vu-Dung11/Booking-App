import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../../shared/models/api-response.model';
import { AdminDashboardStats, AdminRecentActivities } from '../models/admin-stats.model';

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/admin/dashboard`;

  stats(): Observable<AdminDashboardStats> {
    return this.http.get<ApiResponse<AdminDashboardStats>>(`${this.apiUrl}/stats`)
      .pipe(map(res => res.data));
  }

  recentActivities(): Observable<AdminRecentActivities> {
    return this.http.get<ApiResponse<AdminRecentActivities>>(`${this.apiUrl}/recent-activities`)
      .pipe(map(res => res.data));
  }
}
