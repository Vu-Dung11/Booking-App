export type UserRole = 'ADMIN' | 'GUEST' | 'HOST';

export interface User {
  id: number;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: UserRole;
}
