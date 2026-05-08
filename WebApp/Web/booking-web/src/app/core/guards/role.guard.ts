import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, UserRole } from '../services/auth.service';

/**
 * Chặn route nếu người dùng không có role yêu cầu.
 * Khi role sai: xoá token và đẩy về /login để buộc đăng nhập lại bằng tài khoản đúng.
 */
export const roleGuard = (role: UserRole): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }
  if (!auth.hasRole(role)) {
    auth.clearToken();
    return router.createUrlTree(['/login'], {
      queryParams: { reason: 'forbidden' }
    });
  }
  return true;
};
