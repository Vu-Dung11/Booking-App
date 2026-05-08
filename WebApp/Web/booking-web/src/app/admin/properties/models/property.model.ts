import { User } from '../../../shared/models/user.model';

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
