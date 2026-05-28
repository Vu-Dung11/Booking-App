import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminUserService } from '../services/admin-user.service';
import { AdminUserDetail } from '../models/admin-user.model';
import { ToastService } from '../../../shared/services/toast.service';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-admin-user-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.css'
})
export class AdminUserDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(AdminUserService);
  private toast = inject(ToastService);

  user = signal<AdminUserDetail | null>(null);
  isLoading = signal(true);
  isProcessing = signal(false);
  showConfirm = signal(false);
  actionMode = signal<'lock' | 'unlock'>('lock');

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/admin/users']);
      return;
    }
    this.load(id);
  }

  load(id: number): void {
    this.isLoading.set(true);
    this.service.getDetail(id).subscribe({
      next: u => {
        this.user.set(u);
        this.isLoading.set(false);
      },
      error: err => {
        this.isLoading.set(false);
        this.toast.error(err.error?.message || 'Khong tai duoc user');
      }
    });
  }

  openLock(): void {
    this.actionMode.set('lock');
    this.showConfirm.set(true);
  }
  openUnlock(): void {
    this.actionMode.set('unlock');
    this.showConfirm.set(true);
  }
  closeConfirm(): void {
    this.showConfirm.set(false);
  }

  submitConfirm(): void {
    const u = this.user();
    if (!u) return;
    this.isProcessing.set(true);
    const obs = this.actionMode() === 'lock' ? this.service.lock(u.id) : this.service.unlock(u.id);
    obs.subscribe({
      next: () => {
        this.isProcessing.set(false);
        this.showConfirm.set(false);
        this.toast.success(this.actionMode() === 'lock' ? 'Da khoa tai khoan' : 'Da mo khoa tai khoan');
        this.load(u.id);
      },
      error: err => {
        this.isProcessing.set(false);
        this.toast.error(err.error?.message || 'Thao tac that bai');
      }
    });
  }

  getInitial(name?: string): string {
    return (name || '?').charAt(0).toUpperCase();
  }

  getRoleClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'badge-admin';
      case 'HOST': return 'badge-host';
      case 'GUEST': return 'badge-guest';
      default: return '';
    }
  }
}
