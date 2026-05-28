export type UserRole = 'ADMIN' | 'HOST' | 'GUEST';

export interface AdminUser {
  id: number;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: UserRole;
  isActive: boolean;
}

export interface AdminUserDetail extends AdminUser {
  propertyCount?: number;
  bookingCount?: number;
  reviewCount?: number;
}

export interface AdminUserStats {
  totalUsers: number;
  totalAdmins: number;
  totalHosts: number;
  totalGuests: number;
  lockedUsers: number;
  activeUsers: number;
}

export interface AdminUserCreateRequest {
  email: string;
  password: string;
  fullName: string;
  phoneNumber?: string;
  role: UserRole;
}

export interface AdminUserUpdateRequest {
  fullName: string;
  phoneNumber?: string;
  role: UserRole;
}
