import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminDashboardService } from './services/admin-dashboard.service';
import { AdminDashboardStats, AdminRecentActivities } from './models/admin-stats.model';
import { AnimateOnScrollDirective } from '../../shared/directives/animate-on-scroll.directive';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private service = inject(AdminDashboardService);
  private toast = inject(ToastService);

  stats = signal<AdminDashboardStats | null>(null);
  activities = signal<AdminRecentActivities | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading.set(true);
    this.service.stats().subscribe({
      next: s => this.stats.set(s),
      error: err => this.toast.error(err.error?.message || 'Khong tai duoc thong ke')
    });
    this.service.recentActivities().subscribe({
      next: a => {
        this.activities.set(a);
        this.isLoading.set(false);
      },
      error: err => {
        this.isLoading.set(false);
        this.toast.error(err.error?.message || 'Khong tai duoc hoat dong gan day');
      }
    });
  }

  formatNumber(n: number): string {
    if (n == null) return '0';
    return new Intl.NumberFormat('vi-VN').format(n);
  }

  formatPrice(n: number): string {
    if (n == null) return '0 VND';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n);
  }
}
