import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AnimateOnScrollDirective } from '../../shared/directives/animate-on-scroll.directive';
import { DashboardService, HostDashboardStats } from './services/dashboard.service';
import { BookingService } from '../bookings/services/booking.service';
import { Booking } from '../bookings/models/booking.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private bookingService = inject(BookingService);

  stats = signal<HostDashboardStats>({
    totalProperties: 0,
    activeProperties: 0,
    totalBookings: 0,
    pendingBookings: 0,
    confirmedBookings: 0,
    completedBookings: 0,
    cancelledBookings: 0,
    totalReviews: 0,
    averageRating: 0,
    totalRevenue: 0
  });

  recentBookings = signal<Booking[]>([]);
  isLoading = signal(true);

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    forkJoin({
      stats: this.dashboardService.getStats(),
      recent: this.bookingService.getBookings(0, 5)
    }).subscribe({
      next: ({ stats, recent }) => {
        this.stats.set(stats);
        this.recentBookings.set(recent.content ?? []);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
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

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      'PENDING':   'Chờ thanh toán',
      'CONFIRMED': 'Đã xác nhận',
      'COMPLETED': 'Hoàn thành',
      'CANCELLED': 'Đã huỷ',
      'REFUNDED':  'Đã hoàn tiền',
      'PAID':      'Đã thanh toán',
    };
    return map[status] ?? status;
  }

  formatPrice(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
  }

  formatDate(date: string): string {
    return date ? new Date(date).toLocaleDateString('vi-VN') : '';
  }
}
