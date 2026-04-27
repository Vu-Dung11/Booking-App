import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, Page } from '../../shared/models/api-response.model';
import { AnimateOnScrollDirective } from '../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private http = inject(HttpClient);

  stats = signal({
    totalProperties: 0,
    totalBookings: 0,
    totalUsers: 0,
    pendingBookings: 0,
    confirmedBookings: 0,
    completedBookings: 0,
    cancelledBookings: 0
  });

  recentBookings = signal<any[]>([]);
  isLoading = signal(true);

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    forkJoin({
      properties: this.http.get<ApiResponse<Page<any>>>(`${environment.apiUrl}/properties?page=0&size=1`),
      bookings: this.http.get<ApiResponse<Page<any>>>(`${environment.apiUrl}/bookings?page=0&size=5`),
      users: this.http.get<ApiResponse<Page<any>>>(`${environment.apiUrl}/users?page=0&size=1`),
      pendingBookings: this.http.get<ApiResponse<Page<any>>>(`${environment.apiUrl}/bookings?status=PENDING&page=0&size=1`),
      confirmedBookings: this.http.get<ApiResponse<Page<any>>>(`${environment.apiUrl}/bookings?status=CONFIRMED&page=0&size=1`),
      completedBookings: this.http.get<ApiResponse<Page<any>>>(`${environment.apiUrl}/bookings?status=COMPLETED&page=0&size=1`),
      cancelledBookings: this.http.get<ApiResponse<Page<any>>>(`${environment.apiUrl}/bookings?status=CANCELLED&page=0&size=1`)
    }).subscribe({
      next: (res) => {
        this.stats.set({
          totalProperties: res.properties.data?.totalElements ?? 0,
          totalBookings: res.bookings.data?.totalElements ?? 0,
          totalUsers: res.users.data?.totalElements ?? 0,
          pendingBookings: res.pendingBookings.data?.totalElements ?? 0,
          confirmedBookings: res.confirmedBookings.data?.totalElements ?? 0,
          completedBookings: res.completedBookings.data?.totalElements ?? 0,
          cancelledBookings: res.cancelledBookings.data?.totalElements ?? 0
        });
        this.recentBookings.set(res.bookings.data?.content ?? []);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'badge-warning';
      case 'CONFIRMED': return 'badge-primary';
      case 'COMPLETED': return 'badge-success';
      case 'CANCELLED': return 'badge-danger';
      default: return '';
    }
  }
}
