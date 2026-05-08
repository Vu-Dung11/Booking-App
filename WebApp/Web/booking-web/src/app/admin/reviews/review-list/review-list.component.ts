import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReviewService } from '../services/review.service';
import { Review } from '../models/review.model';
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
  private reviewService = inject(ReviewService);

  reviews = signal<Review[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;

  ngOnInit(): void {
    this.loadReviews();
  }

  loadReviews(): void {
    this.isLoading.set(true);
    this.reviewService.getReviews(this.currentPage(), this.pageSize).subscribe({
      next: (page: Page<Review>) => {
        this.reviews.set(page.content);
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
      this.loadReviews();
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  formatDate(date: string): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('vi-VN');
  }

  getStars(rating: number): number[] {
    return Array.from({ length: Math.max(0, Math.min(5, rating)) }, (_, i) => i);
  }
}
