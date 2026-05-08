import { Component, signal, inject, HostListener, PLATFORM_ID, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  sidebarOpen = signal(true);
  mobileMenuOpen = signal(false);
  isMobile = signal(false);

  currentUser = this.authService.currentUser;

  navItems = [
    { path: '/host/dashboard', label: 'Tong quan', icon: 'dashboard' },
    { path: '/host/properties', label: 'Homestay cua toi', icon: 'property' },
    { path: '/host/bookings', label: 'Dat phong', icon: 'booking' },
    { path: '/host/payments', label: 'Thanh toan', icon: 'payment' },
    { path: '/host/reviews', label: 'Danh gia', icon: 'review' },
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
    return user?.sub?.charAt(0)?.toUpperCase() || 'H';
  }
}
