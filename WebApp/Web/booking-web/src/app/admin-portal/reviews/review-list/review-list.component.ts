import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminReviewService } from '../services/admin-review.service';
import { AdminReview } from '../models/admin-review.model';
import { Page } from '../../../shared/models/api-response.model';
import { ToastService } from '../../../shared/services/toast.service';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-admin-review-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css'
})
export class AdminReviewListComponent implements OnInit {
  private service = inject(AdminReviewService);
  private toast = inject(ToastService);

  reviews = signal<AdminReview[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;

  selectedRating: number | 'ALL' = 'ALL';
  keyword = '';

  showConfirm = signal(false);
  actionTarget = signal<AdminReview | null>(null);
  isProcessing = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.service.list(
      this.currentPage(),
      this.pageSize,
      this.selectedRating,
      undefined,
      undefined,
      this.keyword
    ).subscribe({
      next: (page: Page<AdminReview>) => {
        this.reviews.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.toast.error('Khong tai duoc danh sach review');
      }
    });
  }

  onFilterChange(): void {
    this.currentPage.set(0);
    this.load();
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages()) {
      this.currentPage.set(p);
      this.load();
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  openDelete(r: AdminReview): void {
    this.actionTarget.set(r);
    this.showConfirm.set(true);
  }

  closeConfirm(): void {
    this.showConfirm.set(false);
    this.actionTarget.set(null);
  }

  submitDelete(): void {
    const r = this.actionTarget();
    if (!r) return;
    this.isProcessing.set(true);
    this.service.delete(r.id).subscribe({
      next: () => {
        this.isProcessing.set(false);
        this.showConfirm.set(false);
        this.toast.success('Da xoa review');
        this.load();
      },
      error: err => {
        this.isProcessing.set(false);
        this.toast.error(err.error?.message || 'Xoa that bai');
      }
    });
  }

  truncate(text: string | undefined, max = 100): string {
    if (!text) return '';
    return text.length > max ? text.slice(0, max) + '…' : text;
  }

  formatDate(d?: string): string {
    if (!d) return '';
    return new Date(d).toLocaleDateString('vi-VN');
  }

  getInitial(name?: string): string {
    return (name || '?').charAt(0).toUpperCase();
  }

  stars(n: number): string {
    return '★'.repeat(n) + '☆'.repeat(5 - n);
  }
}
