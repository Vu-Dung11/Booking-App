export interface User {
  id: number;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: 'ADMIN' | 'GUEST' | 'HOST';
}
