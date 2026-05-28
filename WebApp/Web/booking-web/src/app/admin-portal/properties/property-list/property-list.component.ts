import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AdminPropertyService } from '../services/admin-property.service';
import { AdminProperty } from '../models/admin-property.model';
import { Page } from '../../../shared/models/api-response.model';
import { ToastService } from '../../../shared/services/toast.service';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-admin-property-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './property-list.component.html',
  styleUrl: './property-list.component.css'
})
export class AdminPropertyListComponent implements OnInit {
  private service = inject(AdminPropertyService);
  private toast = inject(ToastService);

  properties = signal<AdminProperty[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 12;

  cityFilter = '';
  selectedActive: boolean | 'ALL' = 'ALL';
  keyword = '';

  showConfirm = signal(false);
  confirmMode = signal<'toggle' | 'delete'>('toggle');
  actionTarget = signal<AdminProperty | null>(null);
  isProcessing = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.service.list(
      this.currentPage(),
      this.pageSize,
      this.cityFilter,
      undefined,
      this.selectedActive,
      this.keyword
    ).subscribe({
      next: (page: Page<AdminProperty>) => {
        this.properties.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.toast.error('Khong tai duoc danh sach homestay');
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

  openToggle(p: AdminProperty): void {
    this.actionTarget.set(p);
    this.confirmMode.set('toggle');
    this.showConfirm.set(true);
  }

  openDelete(p: AdminProperty): void {
    this.actionTarget.set(p);
    this.confirmMode.set('delete');
    this.showConfirm.set(true);
  }

  closeConfirm(): void {
    this.showConfirm.set(false);
    this.actionTarget.set(null);
  }

  submitConfirm(): void {
    const p = this.actionTarget();
    if (!p) return;
    this.isProcessing.set(true);
    const obs: Observable<unknown> = this.confirmMode() === 'toggle'
      ? this.service.toggleActive(p.id)
      : this.service.softDelete(p.id);
    obs.subscribe({
      next: () => {
        this.isProcessing.set(false);
        this.showConfirm.set(false);
        this.toast.success(
          this.confirmMode() === 'toggle'
            ? 'Da cap nhat trang thai homestay'
            : 'Da an homestay khoi he thong'
        );
        this.load();
      },
      error: (err: any) => {
        this.isProcessing.set(false);
        this.toast.error(err.error?.message || 'Thao tac that bai');
      }
    });
  }

  formatRating(r?: number): string {
    if (!r) return '—';
    return r.toFixed(1);
  }
}
