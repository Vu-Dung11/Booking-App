import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Booking } from '../models/booking.model';

@Injectable({
  providedIn: 'root'
})
export class BookingService {

  private mockBookings: Booking[] = [
    {
      id: 101,
      guest: { id: 3, email: 'guest1@booking.com', fullName: 'Jane Smith Guest', role: 'GUEST' },
      room: {
        id: 1,
        property: { id: 1, host: { id: 2, email: 'host1@booking.com', fullName: 'John Doe Host', role: 'HOST' }, name: 'Luxury Ocean View Villa', description: '', address: '', city: 'Da Nang', country: 'Vietnam', isActive: true },
        roomType: 'Deluxe Suite',
        capacity: 4,
        basePrice: 150.00,
        quantity: 2
      },
      checkInDate: '2025-05-10',
      checkOutDate: '2025-05-15',
      totalPrice: 750.00,
      roomQuantity: 1,
      status: 'CONFIRMED',
      createdAt: '2025-04-01T10:00:00Z'
    },
    {
      id: 102,
      guest: { id: 5, email: 'guest2@booking.com', fullName: 'Bob Guest', role: 'GUEST' },
      room: {
        id: 2,
        property: { id: 2, host: { id: 4, email: 'host2@booking.com', fullName: 'Alice Host', role: 'HOST' }, name: 'Cozy Mountain Cabin', description: '', address: '', city: 'Da Lat', country: 'Vietnam', isActive: true },
        roomType: 'Standard Room',
        capacity: 2,
        basePrice: 80.00,
        quantity: 5
      },
      checkInDate: '2025-06-01',
      checkOutDate: '2025-06-05',
      totalPrice: 320.00,
      roomQuantity: 1,
      status: 'PENDING',
      createdAt: '2025-05-20T14:30:00Z'
    },
    {
      id: 103,
      guest: { id: 3, email: 'guest1@booking.com', fullName: 'Jane Smith Guest', role: 'GUEST' },
      room: {
        id: 3,
        property: { id: 3, host: { id: 2, email: 'host1@booking.com', fullName: 'John Doe Host', role: 'HOST' }, name: 'Modern City Apartment', description: '', address: '', city: 'Ho Chi Minh', country: 'Vietnam', isActive: false },
        roomType: 'Studio',
        capacity: 2,
        basePrice: 100.00,
        quantity: 1
      },
      checkInDate: '2024-12-10',
      checkOutDate: '2024-12-15',
      totalPrice: 500.00,
      roomQuantity: 1,
      status: 'COMPLETED',
      createdAt: '2024-11-01T08:15:00Z'
    },
    {
      id: 104,
      guest: { id: 5, email: 'guest2@booking.com', fullName: 'Bob Guest', role: 'GUEST' },
      room: {
        id: 1,
        property: { id: 1, host: { id: 2, email: 'host1@booking.com', fullName: 'John Doe Host', role: 'HOST' }, name: 'Luxury Ocean View Villa', description: '', address: '', city: 'Da Nang', country: 'Vietnam', isActive: true },
        roomType: 'Deluxe Suite',
        capacity: 4,
        basePrice: 150.00,
        quantity: 2
      },
      checkInDate: '2025-07-20',
      checkOutDate: '2025-07-25',
      totalPrice: 750.00,
      roomQuantity: 1,
      status: 'CANCELLED',
      createdAt: '2025-06-10T11:45:00Z'
    }
  ];

  constructor(private http: HttpClient) { }

  getBookings(): Observable<Booking[]> {
    return of(this.mockBookings);
  }
}
