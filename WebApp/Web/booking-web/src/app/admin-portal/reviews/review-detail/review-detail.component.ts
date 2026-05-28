import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminReviewService } from '../services/admin-review.service';
import { AdminReviewDetail } from '../models/admin-review.model';
import { ToastService } from '../../../shared/services/toast.service';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-admin-review-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css'
})
export class AdminReviewDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(AdminReviewService);
  private toast = inject(ToastService);

  detail = signal<AdminReviewDetail | null>(null);
  isLoading = signal(true);
  isProcessing = signal(false);
  showConfirm = signal(false);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/admin/reviews']);
      return;
    }
    this.load(id);
  }

  load(id: number): void {
    this.isLoading.set(true);
    this.service.getDetail(id).subscribe({
      next: d => {
        this.detail.set(d);
        this.isLoading.set(false);
      },
      error: err => {
        this.isLoading.set(false);
        this.toast.error(err.error?.message || 'Khong tai duoc review');
      }
    });
  }

  openDelete(): void {
    this.showConfirm.set(true);
  }

  closeConfirm(): void {
    this.showConfirm.set(false);
  }

  submitDelete(): void {
    const d = this.detail();
    if (!d) return;
    this.isProcessing.set(true);
    this.service.delete(d.id).subscribe({
      next: () => {
        this.isProcessing.set(false);
        this.toast.success('Da xoa review');
        this.router.navigate(['/admin/reviews']);
      },
      error: err => {
        this.isProcessing.set(false);
        this.toast.error(err.error?.message || 'Xoa that bai');
      }
    });
  }

  stars(n: number): string {
    return '★'.repeat(n) + '☆'.repeat(5 - n);
  }

  formatDate(d?: string): string {
    if (!d) return '—';
    return new Date(d).toLocaleString('vi-VN');
  }

  formatDateShort(d?: string): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('vi-VN');
  }

  formatPrice(n: number): string {
    if (n == null) return '0 VND';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n);
  }

  getInitial(name?: string): string {
    return (name || '?').charAt(0).toUpperCase();
  }
}
