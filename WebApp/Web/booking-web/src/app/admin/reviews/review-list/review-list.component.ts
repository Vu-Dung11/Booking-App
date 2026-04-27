import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookingService } from '../../bookings/services/booking.service';
import { Booking } from '../../bookings/models/booking.model';
import { Page } from '../../../shared/models/api-response.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-review-list',
  standalone: true,
  imports: [CommonModule, AnimateOnScrollDirective],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css'
})
export class ReviewListComponent implements OnInit {
  private bookingService = inject(BookingService);

  bookings = signal<Booking[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;

  ngOnInit(): void {
    this.loadCompletedBookings();
  }

  loadCompletedBookings(): void {
    this.isLoading.set(true);
    this.bookingService.getBookings(this.currentPage(), this.pageSize, 'COMPLETED').subscribe({
      next: (page: Page<Booking>) => {
        this.bookings.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadCompletedBookings();
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('vi-VN');
  }
}
