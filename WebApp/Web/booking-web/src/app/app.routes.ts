import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'host',
    loadComponent: () => import('./admin/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [authGuard, roleGuard('HOST')],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./admin/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'properties',
        loadComponent: () => import('./admin/properties/property-list/property-list.component').then(m => m.PropertyListComponent)
      },
      {
        path: 'properties/:id',
        loadComponent: () => import('./admin/properties/property-detail/property-detail.component').then(m => m.PropertyDetailComponent)
      },
      {
        path: 'properties/:id/calendar',
        loadComponent: () => import('./admin/properties/calendar/calendar.component').then(m => m.CalendarComponent)
      },
      {
        path: 'bookings',
        loadComponent: () => import('./admin/bookings/booking-list/booking-list.component').then(m => m.BookingListComponent)
      },
      {
        path: 'bookings/:id',
        loadComponent: () => import('./admin/bookings/booking-detail/booking-detail.component').then(m => m.BookingDetailComponent)
      },
      {
        path: 'payments',
        loadComponent: () => import('./admin/payments/payment-list/payment-list.component').then(m => m.PaymentListComponent)
      },
      {
        path: 'reviews',
        loadComponent: () => import('./admin/reviews/review-list/review-list.component').then(m => m.ReviewListComponent)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  // Admin portal — quan tri vien he thong
  {
    path: 'admin',
    loadComponent: () => import('./admin-portal/admin-layout/admin-portal-layout.component').then(m => m.AdminPortalLayoutComponent),
    canActivate: [authGuard, roleGuard('ADMIN')],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./admin-portal/dashboard/dashboard.component').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./admin-portal/users/user-list/user-list.component').then(m => m.AdminUserListComponent)
      },
      {
        path: 'users/:id',
        loadComponent: () => import('./admin-portal/users/user-detail/user-detail.component').then(m => m.AdminUserDetailComponent)
      },
      {
        path: 'properties',
        loadComponent: () => import('./admin-portal/properties/property-list/property-list.component').then(m => m.AdminPropertyListComponent)
      },
      {
        path: 'properties/:id',
        loadComponent: () => import('./admin-portal/properties/property-detail/property-detail.component').then(m => m.AdminPropertyDetailComponent)
      },
      {
        path: 'reviews',
        loadComponent: () => import('./admin-portal/reviews/review-list/review-list.component').then(m => m.AdminReviewListComponent)
      },
      {
        path: 'reviews/:id',
        loadComponent: () => import('./admin-portal/reviews/review-detail/review-detail.component').then(m => m.AdminReviewDetailComponent)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
