import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminUserService } from '../services/admin-user.service';
import {
  AdminUser,
  AdminUserCreateRequest,
  AdminUserUpdateRequest,
  UserRole
} from '../models/admin-user.model';
import { Page } from '../../../shared/models/api-response.model';
import { ToastService } from '../../../shared/services/toast.service';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

type FormMode = 'create' | 'edit';

interface UserFormState {
  email: string;
  password: string;
  fullName: string;
  phoneNumber: string;
  role: UserRole;
}

@Component({
  selector: 'app-admin-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css'
})
export class AdminUserListComponent implements OnInit {
  private service = inject(AdminUserService);
  private toast = inject(ToastService);

  users = signal<AdminUser[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 10;

  selectedRole: UserRole | 'ALL' = 'ALL';
  selectedActive: boolean | 'ALL' = 'ALL';
  keyword = '';

  // Lock/unlock modal (giữ nguyên)
  showConfirm = signal(false);
  actionTarget = signal<AdminUser | null>(null);
  actionMode = signal<'lock' | 'unlock'>('lock');
  isProcessing = signal(false);

  // Create / Edit form modal
  showForm = signal(false);
  formMode = signal<FormMode>('create');
  formTarget = signal<AdminUser | null>(null);
  formError = signal<string | null>(null);
  isSubmittingForm = signal(false);
  form: UserFormState = this.blankForm();

  // Delete confirm modal
  showDelete = signal(false);
  deleteTarget = signal<AdminUser | null>(null);
  isDeleting = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.service.list(
      this.currentPage(),
      this.pageSize,
      this.selectedRole,
      this.selectedActive,
      this.keyword
    ).subscribe({
      next: (page: Page<AdminUser>) => {
        this.users.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.toast.error('Khong tai duoc danh sach user');
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

  // ─── Lock / Unlock ────────────────────────────────────────────────

  openLock(u: AdminUser): void {
    this.actionTarget.set(u);
    this.actionMode.set('lock');
    this.showConfirm.set(true);
  }

  openUnlock(u: AdminUser): void {
    this.actionTarget.set(u);
    this.actionMode.set('unlock');
    this.showConfirm.set(true);
  }

  closeConfirm(): void {
    this.showConfirm.set(false);
    this.actionTarget.set(null);
  }

  submitConfirm(): void {
    const u = this.actionTarget();
    if (!u) return;
    this.isProcessing.set(true);
    const obs = this.actionMode() === 'lock' ? this.service.lock(u.id) : this.service.unlock(u.id);
    obs.subscribe({
      next: () => {
        this.isProcessing.set(false);
        this.showConfirm.set(false);
        this.toast.success(
          this.actionMode() === 'lock'
            ? `Da khoa tai khoan ${u.email}`
            : `Da mo khoa tai khoan ${u.email}`
        );
        this.load();
      },
      error: (err) => {
        this.isProcessing.set(false);
        this.toast.error(err.error?.message || 'Thao tac that bai');
      }
    });
  }

  // ─── Create / Edit form ───────────────────────────────────────────

  openCreate(): void {
    this.formMode.set('create');
    this.formTarget.set(null);
    this.form = this.blankForm();
    this.formError.set(null);
    this.showForm.set(true);
  }

  openEdit(u: AdminUser): void {
    this.formMode.set('edit');
    this.formTarget.set(u);
    this.form = {
      email: u.email,
      password: '',
      fullName: u.fullName,
      phoneNumber: u.phoneNumber ?? '',
      role: u.role
    };
    this.formError.set(null);
    this.showForm.set(true);
  }

  closeForm(): void {
    if (this.isSubmittingForm()) return;
    this.showForm.set(false);
    this.formTarget.set(null);
    this.formError.set(null);
  }

  submitForm(): void {
    this.formError.set(null);

    if (!this.form.fullName.trim()) {
      this.formError.set('Vui long nhap ho ten');
      return;
    }
    if (this.formMode() === 'create') {
      if (!this.form.email.trim()) { this.formError.set('Vui long nhap email'); return; }
      if (this.form.password.length < 6) { this.formError.set('Mat khau toi thieu 6 ky tu'); return; }
    }

    this.isSubmittingForm.set(true);
    const obs = this.formMode() === 'create'
      ? this.service.create({
          email: this.form.email.trim(),
          password: this.form.password,
          fullName: this.form.fullName.trim(),
          phoneNumber: this.form.phoneNumber.trim() || undefined,
          role: this.form.role
        } as AdminUserCreateRequest)
      : this.service.update(this.formTarget()!.id, {
          fullName: this.form.fullName.trim(),
          phoneNumber: this.form.phoneNumber.trim() || undefined,
          role: this.form.role
        } as AdminUserUpdateRequest);

    obs.subscribe({
      next: (saved) => {
        this.isSubmittingForm.set(false);
        this.showForm.set(false);
        this.toast.success(
          this.formMode() === 'create'
            ? `Da tao tai khoan ${saved.email}`
            : `Da cap nhat tai khoan ${saved.email}`
        );
        this.load();
      },
      error: (err) => {
        this.isSubmittingForm.set(false);
        this.formError.set(err.error?.message || 'Thao tac that bai');
      }
    });
  }

  // ─── Delete ───────────────────────────────────────────────────────

  openDelete(u: AdminUser): void {
    this.deleteTarget.set(u);
    this.showDelete.set(true);
  }

  closeDelete(): void {
    if (this.isDeleting()) return;
    this.showDelete.set(false);
    this.deleteTarget.set(null);
  }

  submitDelete(): void {
    const u = this.deleteTarget();
    if (!u) return;
    this.isDeleting.set(true);
    this.service.delete(u.id).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.showDelete.set(false);
        this.toast.success(`Da xoa tai khoan ${u.email}`);
        this.load();
      },
      error: (err) => {
        this.isDeleting.set(false);
        this.toast.error(err.error?.message || 'Khong xoa duoc tai khoan');
      }
    });
  }

  // ─── Helpers ──────────────────────────────────────────────────────

  getRoleClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'badge-admin';
      case 'HOST': return 'badge-host';
      case 'GUEST': return 'badge-guest';
      default: return '';
    }
  }

  getInitial(name: string): string {
    return (name || '?').charAt(0).toUpperCase();
  }

  private blankForm(): UserFormState {
    return { email: '', password: '', fullName: '', phoneNumber: '', role: 'GUEST' };
  }
}
