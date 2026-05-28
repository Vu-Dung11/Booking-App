import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { AdminPropertyService } from '../services/admin-property.service';
import { AdminPropertyDetail } from '../models/admin-property.model';
import { ToastService } from '../../../shared/services/toast.service';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-admin-property-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './property-detail.component.html',
  styleUrl: './property-detail.component.css'
})
export class AdminPropertyDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(AdminPropertyService);
  private toast = inject(ToastService);

  detail = signal<AdminPropertyDetail | null>(null);
  isLoading = signal(true);
  isProcessing = signal(false);

  showConfirm = signal(false);
  confirmMode = signal<'toggle' | 'delete'>('toggle');

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/admin/properties']);
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
        this.toast.error(err.error?.message || 'Khong tai duoc homestay');
      }
    });
  }

  openToggle(): void {
    this.confirmMode.set('toggle');
    this.showConfirm.set(true);
  }

  openDelete(): void {
    this.confirmMode.set('delete');
    this.showConfirm.set(true);
  }

  closeConfirm(): void {
    this.showConfirm.set(false);
  }

  submitConfirm(): void {
    const d = this.detail();
    if (!d) return;
    this.isProcessing.set(true);
    const obs: Observable<unknown> = this.confirmMode() === 'toggle'
      ? this.service.toggleActive(d.id)
      : this.service.softDelete(d.id);
    obs.subscribe({
      next: () => {
        this.isProcessing.set(false);
        this.showConfirm.set(false);
        this.toast.success(this.confirmMode() === 'toggle' ? 'Da cap nhat trang thai' : 'Da an homestay');
        this.load(d.id);
      },
      error: (err: any) => {
        this.isProcessing.set(false);
        this.toast.error(err.error?.message || 'Thao tac that bai');
      }
    });
  }

  formatPrice(n: number): string {
    if (n == null) return '0 VND';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n);
  }
}
