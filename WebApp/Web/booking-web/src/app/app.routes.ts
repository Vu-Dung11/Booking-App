import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin/admin-layout/admin-layout.component';

export const routes: Routes = [
  {
    path: 'admin',
    component: AdminLayoutComponent,
    children: [
      {
        path: 'users',
        loadComponent: () => import('./admin/users/user-list/user-list.component').then(m => m.UserListComponent)
      },
      {
        path: 'properties',
        loadComponent: () => import('./admin/properties/property-list/property-list.component').then(m => m.PropertyListComponent)
      },
      {
        path: 'bookings',
        loadComponent: () => import('./admin/bookings/booking-list/booking-list.component').then(m => m.BookingListComponent)
      },
      {
        path: '',
        redirectTo: 'users',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '',
    redirectTo: 'admin',
    pathMatch: 'full'
  }
];
