export interface AdminProperty {
  id: number;
  name: string;
  address: string;
  city: string;
  country: string;
  isActive: boolean;
  hostId: number;
  hostEmail: string;
  hostFullName: string;
  thumbnailUrl?: string;
  averageRating?: number;
  reviewCount?: number;
  roomCount?: number;
}

export interface AdminPropertyRoom {
  id: number;
  roomType: string;
  capacity: number;
  basePrice: number;
  quantity: number;
  thumbnailUrl?: string;
}

export interface AdminPropertyDetail {
  id: number;
  name: string;
  description?: string;
  address: string;
  city: string;
  country: string;
  isActive: boolean;
  hostId: number;
  hostEmail: string;
  hostFullName: string;
  hostPhone?: string;
  images: string[];
  rooms: AdminPropertyRoom[];
  totalReviews: number;
  averageRating: number;
  totalBookings: number;
}
