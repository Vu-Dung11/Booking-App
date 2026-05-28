export interface AdminDashboardStats {
  totalUsers: number;
  totalAdmins: number;
  totalHosts: number;
  totalGuests: number;
  lockedUsers: number;
  totalProperties: number;
  activeProperties: number;
  inactiveProperties: number;
  totalBookings: number;
  pendingBookings: number;
  confirmedBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  totalReviews: number;
  totalRevenue: number;
}

export interface AdminRecentItem {
  type: 'USER' | 'BOOKING' | 'REVIEW';
  id: number;
  title: string;
  subtitle?: string;
  createdAt?: string;
}

export interface AdminRecentActivities {
  recentUsers: AdminRecentItem[];
  recentBookings: AdminRecentItem[];
  recentReviews: AdminRecentItem[];
}
