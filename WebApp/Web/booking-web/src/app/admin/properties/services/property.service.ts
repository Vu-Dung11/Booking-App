import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Page } from '../../../shared/models/api-response.model';
import { Property } from '../models/property.model';

export interface PropertyDetailResponse {
  propertyId: number;
  name: string;
  description: string;
  address: string;
  city: string;
  country: string;
  rooms: RoomSearchResponse[];
}

export interface RoomSearchResponse {
  roomId: number;
  roomType: string;
  price: number;
  capacity: number;
}

export interface PropertyRequest {
  name: string;
  description: string;
  address: string;
  city: string;
  country: string;
}

export interface RoomRequest {
  roomType: string;
  capacity: number;
  basePrice: number;
  quantity: number;
}

@Injectable({ providedIn: 'root' })
export class PropertyService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/properties`;

  getProperties(page = 0, size = 10): Observable<Page<Property>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<Page<Property>>>(this.apiUrl, { params }).pipe(
      map(res => res.data)
    );
  }

  getPropertyById(id: number): Observable<Property> {
    return this.http.get<ApiResponse<Property>>(`${this.apiUrl}/${id}`).pipe(
      map(res => res.data)
    );
  }

  getPropertyDetail(id: number): Observable<PropertyDetailResponse> {
    return this.http.get<ApiResponse<PropertyDetailResponse>>(`${this.apiUrl}/${id}/detail`).pipe(
      map(res => res.data)
    );
  }

  createProperty(request: PropertyRequest): Observable<Property> {
    return this.http.post<ApiResponse<Property>>(`${this.apiUrl}/create`, request).pipe(
      map(res => res.data)
    );
  }

  addRoom(propertyId: number, request: RoomRequest): Observable<any> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${propertyId}/rooms`, request).pipe(
      map(res => res.data)
    );
  }

  uploadImages(propertyId: number, files: File[]): Observable<any> {
    const formData = new FormData();
    files.forEach(f => formData.append('files', f));
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${propertyId}/images`, formData).pipe(
      map(res => res.data)
    );
  }
}
