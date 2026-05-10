import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PLATFORM_ID } from '@angular/core';
import { BookingService } from '../services/booking.service';
import {
  BookingDetailResponse,
  ConfirmBookingRequest,
  PaymentMethod
} from '../models/booking.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './booking-detail.component.html',
  styleUrl: './booking-detail.component.css'
})
export class BookingDetailComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private bookingService = inject(BookingService);
  private toast = inject(ToastService);
  private platformId = inject(PLATFORM_ID);

  bookingId = 0;
  booking = signal<BookingDetailResponse | null>(null);
  isLoading = signal(true);

  // Confirm modal
  showConfirmModal = signal(false);
  confirmForm: ConfirmBookingRequest = { paymentMethod: 'CASH', transactionId: '', note: '' };
  isConfirming = signal(false);

  // Cancel modal
  showCancelModal = signal(false);
  cancelReason = '';
  isCancelling = signal(false);

  // Countdown for PENDING bookings
  countdownDisplay = signal('');
  private countdownTimer: any = null;

  // Computed helpers
  nights = computed(() => {
    const b = this.booking();
    if (!b) return 0;
    const ms = new Date(b.checkOutDate).getTime() - new Date(b.checkInDate).getTime();
    return Math.max(1, Math.round(ms / (1000 * 60 * 60 * 24)));
  });

  ngOnInit(): void {
    this.bookingId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadDetail();
  }

  ngOnDestroy(): void {
    this.stopCountdown();
  }

  loadDetail(): void {
    this.isLoading.set(true);
    this.bookingService.getBookingDetail(this.bookingId).subscribe({
      next: (data) => {
        this.booking.set(data);
        this.isLoading.set(false);
        this.startCountdownIfNeeded();
      },
      error: (err) => {
        this.isLoading.set(false);
        this.toast.error(err.error?.message || 'Không thể tải thông tin đặt phòng');
      }
    });
  }

  // ===================== Countdown =====================

  private startCountdownIfNeeded(): void {
    this.stopCountdown();
    if (!isPlatformBrowser(this.platformId)) return;
    const b = this.booking();
    if (!b || b.status !== 'PENDING' || !b.pendingExpiresAt) {
      this.countdownDisplay.set('');
      return;
    }
    this.tickCountdown();
    this.countdownTimer = setInterval(() => this.tickCountdown(), 1000);
  }

  private tickCountdown(): void {
    const b = this.booking();
    if (!b?.pendingExpiresAt) return;
    const remaining = new Date(b.pendingExpiresAt).getTime() - Date.now();
    if (remaining <= 0) {
      this.countdownDisplay.set('Đã hết hạn');
      this.stopCountdown();
      // refresh để lấy status mới (cron có thể đã cancel)
      this.loadDetail();
      return;
    }
    const totalSeconds = Math.floor(remaining / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    this.countdownDisplay.set(
      `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
    );
  }

  private stopCountdown(): void {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
      this.countdownTimer = null;
    }
  }

  // ===================== Confirm =====================

  openConfirmModal(): void {
    this.confirmForm = { paymentMethod: 'CASH', transactionId: '', note: '' };
    this.showConfirmModal.set(true);
  }

  closeConfirmModal(): void {
    this.showConfirmModal.set(false);
  }

  submitConfirm(): void {
    this.isConfirming.set(true);
    this.bookingService.confirmBooking(this.bookingId, this.confirmForm).subscribe({
      next: () => {
        this.isConfirming.set(false);
        this.showConfirmModal.set(false);
        this.toast.success('Đã xác nhận thanh toán cho đơn');
        this.loadDetail();
      },
      error: (err) => {
        this.isConfirming.set(false);
        this.toast.error(err.error?.message || 'Không thể xác nhận đơn');
      }
    });
  }

  // ===================== Cancel =====================

  openCancelModal(): void {
    this.cancelReason = '';
    this.showCancelModal.set(true);
  }

  closeCancelModal(): void {
    this.showCancelModal.set(false);
  }

  submitCancel(): void {
    this.isCancelling.set(true);
    this.bookingService.cancelBooking(this.bookingId, this.cancelReason).subscribe({
      next: () => {
        this.isCancelling.set(false);
        this.showCancelModal.set(false);
        this.toast.success('Đã huỷ đơn đặt phòng');
        this.loadDetail();
      },
      error: (err) => {
        this.isCancelling.set(false);
        this.toast.error(err.error?.message || 'Không thể huỷ đơn');
      }
    });
  }

  // ===================== helpers =====================

  formatPrice(value: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('vi-VN');
  }

  formatDateTime(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleString('vi-VN');
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

  getPaymentStatusClass(status: string): string {
    switch (status) {
      case 'SUCCESS': return 'badge-success';
      case 'PENDING': return 'badge-warning';
      case 'FAILED': return 'badge-danger';
      case 'REFUNDED': return 'badge-muted';
      default: return '';
    }
  }

  getPaymentMethodLabel(method: string): string {
    switch (method) {
      case 'CASH': return 'Tiền mặt';
      case 'BANK_TRANSFER': return 'Chuyển khoản';
      case 'OTHER': return 'Khác';
      case 'VNPAY': case 'VNPAY_MOCK': return 'VNPay';
      default: return method;
    }
  }
}
