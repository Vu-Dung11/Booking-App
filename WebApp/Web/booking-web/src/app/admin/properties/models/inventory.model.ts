export interface DayInventory {
  date: string;            // ISO yyyy-MM-dd
  availableCount: number;
  bookedCount: number;
  hasInventory: boolean;
}

export interface RoomCalendar {
  roomId: number;
  roomType: string;
  capacity: number;
  quantity: number;
  days: DayInventory[];
}

export interface HostCalendarResponse {
  fromDate: string;
  toDate: string;
  rooms: RoomCalendar[];
}

export interface DayInventoryUpdateRequest {
  fromDate: string;
  toDate: string;
  availableCount: number;
}

export interface ExtendInventoryRequest {
  untilDate: string;
}

export interface ExtendInventoryResult {
  created: number;
  lastDate: string;
}

export interface BookingByDateItem {
  id: number;
  guestName: string | null;
  guestPhone: string | null;
  status: 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | string;
  roomQuantity: number;
  checkInDate: string;
  checkOutDate: string;
}
