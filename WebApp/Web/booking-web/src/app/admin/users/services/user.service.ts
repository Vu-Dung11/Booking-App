import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  // private apiUrl = '/api/users';

  // Mock data for initial development until backend is connected
  private mockUsers: User[] = [
    { id: 1, email: 'admin@booking.com', fullName: 'Super Admin', phoneNumber: '0123456789', role: 'ADMIN' },
    { id: 2, email: 'host1@booking.com', fullName: 'John Doe Host', phoneNumber: '0987654321', role: 'HOST' },
    { id: 3, email: 'guest1@booking.com', fullName: 'Jane Smith Guest', phoneNumber: '0345678912', role: 'GUEST' },
    { id: 4, email: 'host2@booking.com', fullName: 'Alice Host', phoneNumber: '0876543210', role: 'HOST' },
    { id: 5, email: 'guest2@booking.com', fullName: 'Bob Guest', phoneNumber: '0912345678', role: 'GUEST' }
  ];

  constructor(private http: HttpClient) { }

  getUsers(): Observable<User[]> {
    // return this.http.get<User[]>(this.apiUrl);
    return of(this.mockUsers);
  }
}
