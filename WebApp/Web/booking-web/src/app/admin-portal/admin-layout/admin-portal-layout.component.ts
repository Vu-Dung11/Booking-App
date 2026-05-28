import { Component, signal, inject, HostListener, PLATFORM_ID, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-portal-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-portal-layout.component.html',
  styleUrl: './admin-portal-layout.component.css'
})
export class AdminPortalLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  sidebarOpen = signal(true);
  mobileMenuOpen = signal(false);
  isMobile = signal(false);

  currentUser = this.authService.currentUser;

  navItems = [
    { path: '/admin/dashboard', label: 'Tong quan', icon: 'dashboard' },
    { path: '/admin/users', label: 'Tai khoan', icon: 'user' },
    { path: '/admin/properties', label: 'Homestays', icon: 'property' },
    { path: '/admin/reviews', label: 'Reviews', icon: 'review' },
  ];

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.checkMobile();
    }
  }

  @HostListener('window:resize')
  onResize(): void {
    this.checkMobile();
  }

  private checkMobile(): void {
    const mobile = window.innerWidth < 1024;
    this.isMobile.set(mobile);
    if (mobile) {
      this.sidebarOpen.set(false);
    } else {
      this.sidebarOpen.set(true);
      this.mobileMenuOpen.set(false);
    }
  }

  toggleSidebar(): void {
    if (this.isMobile()) {
      this.mobileMenuOpen.update(v => !v);
    } else {
      this.sidebarOpen.update(v => !v);
    }
  }

  closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  logout(): void {
    this.authService.logout();
  }

  getUserInitial(): string {
    const user = this.currentUser();
    return user?.sub?.charAt(0)?.toUpperCase() || 'A';
  }
}
