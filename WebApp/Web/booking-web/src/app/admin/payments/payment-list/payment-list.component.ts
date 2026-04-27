import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookingService } from '../../bookings/services/booking.service';
import { Booking } from '../../bookings/models/booking.model';
import { Page } from '../../../shared/models/api-response.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-payment-list',
  standalone: true,
  imports: [CommonModule, FormsModule, AnimateOnScrollDirective],
  templateUrl: './payment-list.component.html',
  styleUrl: './payment-list.component.css'
})
export class PaymentListComponent implements OnInit {
  private bookingService = inject(BookingService);

  bookings = signal<Booking[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;
  selectedStatus: 'CONFIRMED' | 'COMPLETED' | 'ALL' = 'ALL';

  ngOnInit(): void {
    this.loadPayments();
  }

  loadPayments(): void {
    this.isLoading.set(true);
    const status = this.selectedStatus === 'ALL' ? 'CONFIRMED' : this.selectedStatus;
    this.bookingService.getBookings(this.currentPage(), this.pageSize, status).subscribe({
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
    this.loadPayments();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadPayments();
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  getPaymentStatusClass(status: string): string {
    switch (status) {
      case 'CONFIRMED': return 'badge-success';
      case 'COMPLETED': return 'badge-primary';
      default: return '';
    }
  }

  getPaymentLabel(status: string): string {
    switch (status) {
      case 'CONFIRMED': return 'Da thanh toan';
      case 'COMPLETED': return 'Hoan thanh';
      default: return status;
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
