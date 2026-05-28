import { Injectable, signal, computed, PLATFORM_ID, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';

export interface AuthResponse {
  token: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export type UserRole = 'ADMIN' | 'HOST' | 'GUEST';

export interface DecodedToken {
  sub: string;
  role: UserRole | string;
  userId: number;
  exp: number;
}

/** Portal nay phuc vu HOST va ADMIN. */
export const ALLOWED_PORTAL_ROLES: UserRole[] = ['HOST', 'ADMIN'];

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  private tokenSignal = signal<string | null>(this.getStoredToken());

  isAuthenticated = computed(() => {
    const token = this.tokenSignal();
    if (!token) return false;
    const decoded = this.decodeToken(token);
    return decoded ? decoded.exp * 1000 > Date.now() : false;
  });

  currentUser = computed(() => {
    const token = this.tokenSignal();
    if (!token) return null;
    return this.decodeToken(token);
  });

  hasRole(role: UserRole): boolean {
    return this.currentUser()?.role === role;
  }

  isHost = computed(() => this.currentUser()?.role === 'HOST');
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  get token(): string | null {
    return this.tokenSignal();
  }

  /**
   * Đăng nhập. Backend trả token kèm role.
   * Nếu role không phải HOST thì xoá token ngay và trả error code = 403 ở vị trí client
   * để component login hiển thị thông báo phù hợp.
   */
  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${environment.apiUrl}/auth/login`, request
    ).pipe(
      tap(res => {
        if (res.code !== 0 || !res.data?.token) return;
        const decoded = this.decodeToken(res.data.token);
        if (!decoded || !ALLOWED_PORTAL_ROLES.includes(decoded.role as UserRole)) {
          this.clearToken();
          // Mutate response để component nhận biết
          res.code = 403;
          res.message = 'Tài khoản không có quyền truy cập portal này.';
          res.data = null as any;
          return;
        }
        this.setToken(res.data.token);
      })
    );
  }

  logout(): void {
    this.clearToken();
    this.router.navigate(['/login']);
  }

  clearToken(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
    }
    this.tokenSignal.set(null);
  }

  private setToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('token', token);
    }
    this.tokenSignal.set(token);
  }

  private getStoredToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('token');
    }
    return null;
  }

  private decodeToken(token: string): DecodedToken | null {
    try {
      let payload = token.split('.')[1];
      payload = payload.replace(/-/g, '+').replace(/_/g, '/');
      while (payload.length % 4) {
        payload += '=';
      }
      return JSON.parse(atob(payload));
    } catch {
      return null;
    }
  }
}
