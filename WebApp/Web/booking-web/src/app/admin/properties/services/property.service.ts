import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Property } from '../models/property.model';

@Injectable({
  providedIn: 'root'
})
export class PropertyService {

  private mockProperties: Property[] = [
    {
      id: 1,
      host: { id: 2, email: 'host1@booking.com', fullName: 'John Doe Host', role: 'HOST' },
      name: 'Luxury Ocean View Villa',
      description: 'Beautiful 5 bedroom villa with stunning ocean views.',
      address: '123 Beachfront Ave',
      city: 'Da Nang',
      country: 'Vietnam',
      isActive: true
    },
    {
      id: 2,
      host: { id: 4, email: 'host2@booking.com', fullName: 'Alice Host', role: 'HOST' },
      name: 'Cozy Mountain Cabin',
      description: 'A quiet retreat in the mountains.',
      address: '456 Pine Ridge',
      city: 'Da Lat',
      country: 'Vietnam',
      isActive: true
    },
    {
      id: 3,
      host: { id: 2, email: 'host1@booking.com', fullName: 'John Doe Host', role: 'HOST' },
      name: 'Modern City Apartment',
      description: 'Central location, walking distance to everything.',
      address: '789 Downtown Blvd',
      city: 'Ho Chi Minh',
      country: 'Vietnam',
      isActive: false
    }
  ];

  constructor(private http: HttpClient) { }

  getProperties(): Observable<Property[]> {
    return of(this.mockProperties);
  }
}
