import { User } from '../../users/models/user.model';

export interface Property {
  id: number;
  host: User;
  name: string;
  description: string;
  address: string;
  city: string;
  country: string;
  isActive: boolean;
}
