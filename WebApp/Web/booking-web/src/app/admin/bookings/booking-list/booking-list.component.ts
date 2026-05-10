import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BookingService } from '../services/booking.service';
import { Booking, BookingStatus } from '../models/booking.model';
import { Page } from '../../../shared/models/api-response.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-booking-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './booking-list.component.html',
  styleUrl: './booking-list.component.css'
})
export class BookingListComponent implements OnInit {
  private bookingService = inject(BookingService);
  private toast = inject(ToastService);

  bookings = signal<Booking[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;
  selectedStatus: BookingStatus | 'ALL' = 'ALL';

  // Inline action state
  showQuickConfirm = signal(false);
  showQuickCancel = signal(false);
  actionTarget = signal<Booking | null>(null);
  cancelReason = '';
  isProcessingAction = signal(false);

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    this.isLoading.set(true);
    this.bookingService.getBookings(this.currentPage(), this.pageSize, this.selectedStatus).subscribe({
      next: (page: Page<Booking>) => {
        this.bookings.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  onFilterChange(): void {
    this.currentPage.set(0);
    this.loadBookings();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadBookings();
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  // Inline confirm
  openQuickConfirm(b: Booking): void {
    this.actionTarget.set(b);
    this.showQuickConfirm.set(true);
  }

  closeQuickConfirm(): void {
    this.showQuickConfirm.set(false);
    this.actionTarget.set(null);
  }

  submitQuickConfirm(): void {
    const target = this.actionTarget();
    if (!target) return;
    this.isProcessingAction.set(true);
    this.bookingService.confirmBooking(target.id, { paymentMethod: 'CASH' }).subscribe({
      next: () => {
        this.isProcessingAction.set(false);
        this.showQuickConfirm.set(false);
        this.actionTarget.set(null);
        this.toast.success(`Đã xác nhận đơn #${target.id}`);
        this.loadBookings();
      },
      error: (err) => {
        this.isProcessingAction.set(false);
        this.toast.error(err.error?.message || 'Không thể xác nhận đơn');
      }
    });
  }

  // Inline cancel
  openQuickCancel(b: Booking): void {
    this.actionTarget.set(b);
    this.cancelReason = '';
    this.showQuickCancel.set(true);
  }

  closeQuickCancel(): void {
    this.showQuickCancel.set(false);
    this.actionTarget.set(null);
  }

  submitQuickCancel(): void {
    const target = this.actionTarget();
    if (!target) return;
    this.isProcessingAction.set(true);
    this.bookingService.cancelBooking(target.id, this.cancelReason).subscribe({
      next: () => {
        this.isProcessingAction.set(false);
        this.showQuickCancel.set(false);
        this.actionTarget.set(null);
        this.toast.success(`Đã huỷ đơn #${target.id}`);
        this.loadBookings();
      },
      error: (err) => {
        this.isProcessingAction.set(false);
        this.toast.error(err.error?.message || 'Không thể huỷ đơn');
      }
    });
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'Chờ thanh toán';
      case 'CONFIRMED': return 'Đã xác nhận';
      case 'COMPLETED': return 'Đã hoàn tất';
      case 'CANCELLED': return 'Đã huỷ';
      default: return status;
    }
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

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('vi-VN');
  }
}
