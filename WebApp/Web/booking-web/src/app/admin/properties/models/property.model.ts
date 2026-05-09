import { User } from '../../../shared/models/user.model';

export interface Property {
  id: number;
  host?: User;
  name: string;
  description: string;
  address: string;
  city: string;
  country: string;
  isActive: boolean;
  thumbnailUrl?: string | null;
}

export interface PropertyImage {
  id: number;
  imageUrl: string;
  isThumbnail: boolean;
}

export interface RoomImage {
  id: number;
  imageUrl: string;
  isThumbnail: boolean;
}
