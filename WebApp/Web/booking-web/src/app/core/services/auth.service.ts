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

export interface DecodedToken {
  sub: string;
  role: string;
  userId: number;
  exp: number;
}

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

  get token(): string | null {
    return this.tokenSignal();
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${environment.apiUrl}/auth/login`, request
    ).pipe(
      tap(res => {
        if (res.code === 0 && res.data?.token) {
          this.setToken(res.data.token);
        }
      })
    );
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
    }
    this.tokenSignal.set(null);
    this.router.navigate(['/login']);
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
