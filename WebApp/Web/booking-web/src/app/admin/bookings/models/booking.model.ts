import { User } from '../../../shared/models/user.model';
import { Room } from '../../rooms/models/room.model';

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

export interface Booking {
  id: number;
  guest: User;
  room: Room;
  checkInDate: string; // ISO Date string
  checkOutDate: string; // ISO Date string
  totalPrice: number;
  roomQuantity: number;
  status: BookingStatus;
  createdAt: string; // ISO Date string
}
