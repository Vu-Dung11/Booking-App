import { User } from '../../../shared/models/user.model';
import { Room } from '../../rooms/models/room.model';

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export interface Booking {
  id: number;
  guest: User;
  room: Room;
  checkInDate: string;
  checkOutDate: string;
  totalPrice: number;
  roomQuantity: number;
  status: BookingStatus;
  createdAt: string;
}

// ====== Detail DTO from backend BookingDetailResponse ======

export type PaymentMethod = 'CASH' | 'BANK_TRANSFER' | 'OTHER' | 'VNPAY' | 'VNPAY_MOCK' | string;
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

export interface BookingGuestInfo {
  id: number;
  fullName: string;
  email: string;
  phoneNumber?: string | null;
}

export interface BookingRoomInfo {
  id: number;
  roomType: string;
  capacity: number;
  basePrice: number;
  quantity: number;
  thumbnailUrl?: string | null;
}

export interface BookingPropertyInfo {
  id: number;
  name: string;
  address: string;
  city: string;
  country: string;
}

export interface BookingPaymentInfo {
  id: number;
  amount: number;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  transactionId?: string | null;
  createdAt: string;
}

export interface BookingDetailResponse {
  id: number;
  checkInDate: string;
  checkOutDate: string;
  totalPrice: number;
  roomQuantity: number;
  status: BookingStatus;
  createdAt: string;
  pendingExpiresAt?: string | null;
  guest: BookingGuestInfo;
  room: BookingRoomInfo;
  property: BookingPropertyInfo;
  payments: BookingPaymentInfo[];
}

export interface ConfirmBookingRequest {
  paymentMethod?: PaymentMethod;
  transactionId?: string;
  note?: string;
}

export interface CancelBookingRequest {
  reason?: string;
}
