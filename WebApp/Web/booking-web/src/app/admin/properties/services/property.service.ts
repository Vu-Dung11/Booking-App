import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Page } from '../../../shared/models/api-response.model';
import { Property, PropertyImage, RoomImage } from '../models/property.model';

export interface PropertyDetailResponse {
  propetyId: number;
  name: string;
  description: string;
  address: string;
  city: string;
  country: string;
  isActive: boolean;
  thumbnailUrl?: string | null;
  rooms: RoomSearchResponse[];
}

export interface RoomSearchResponse {
  roomId: number;
  roomType: string;
  price: number;
  capacity: number;
  quantity?: number;
  thumbnailUrl?: string | null;
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

/**
 * Tất cả thao tác đều scope theo host hiện tại — backend filter trên host_id
 * lấy từ JWT, frontend không truyền hostId.
 */
@Injectable({ providedIn: 'root' })
export class PropertyService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/host/properties`;

  // ===================== Property CRUD =====================
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
    return this.http.post<ApiResponse<Property>>(this.apiUrl, request).pipe(map(r => r.data));
  }

  updateProperty(id: number, request: PropertyRequest): Observable<Property> {
    return this.http.put<ApiResponse<Property>>(`${this.apiUrl}/${id}`, request).pipe(map(r => r.data));
  }

  deactivateProperty(id: number): Observable<Property> {
    return this.http.patch<ApiResponse<Property>>(`${this.apiUrl}/${id}/deactivate`, {}).pipe(map(r => r.data));
  }

  activateProperty(id: number): Observable<Property> {
    return this.http.patch<ApiResponse<Property>>(`${this.apiUrl}/${id}/activate`, {}).pipe(map(r => r.data));
  }

  deleteProperty(id: number): Observable<string> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${id}`).pipe(map(r => r.data));
  }

  // ===================== Room CRUD =====================
  addRoom(propertyId: number, request: RoomRequest): Observable<any> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/${propertyId}/rooms`, request).pipe(map(r => r.data));
  }

  updateRoom(propertyId: number, roomId: number, request: RoomRequest): Observable<any> {
    return this.http.put<ApiResponse<any>>(`${this.apiUrl}/${propertyId}/rooms/${roomId}`, request).pipe(map(r => r.data));
  }

  deleteRoom(propertyId: number, roomId: number): Observable<string> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${propertyId}/rooms/${roomId}`).pipe(map(r => r.data));
  }

  // ===================== Property images =====================
  uploadPropertyImages(propertyId: number, files: File[]): Observable<PropertyImage[]> {
    const formData = new FormData();
    files.forEach(f => formData.append('files', f));
    return this.http.post<ApiResponse<PropertyImage[]>>(`${this.apiUrl}/${propertyId}/images`, formData)
      .pipe(map(r => r.data));
  }

  getPropertyImages(propertyId: number): Observable<PropertyImage[]> {
    return this.http.get<ApiResponse<PropertyImage[]>>(`${this.apiUrl}/${propertyId}/images`)
      .pipe(map(r => r.data));
  }

  deletePropertyImage(propertyId: number, imageId: number): Observable<string> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${propertyId}/images/${imageId}`)
      .pipe(map(r => r.data));
  }

  setPropertyThumbnail(propertyId: number, imageId: number): Observable<PropertyImage> {
    return this.http.patch<ApiResponse<PropertyImage>>(`${this.apiUrl}/${propertyId}/images/${imageId}/thumbnail`, {})
      .pipe(map(r => r.data));
  }

  // ===================== Room images =====================
  uploadRoomImages(propertyId: number, roomId: number, files: File[]): Observable<RoomImage[]> {
    const formData = new FormData();
    files.forEach(f => formData.append('files', f));
    return this.http.post<ApiResponse<RoomImage[]>>(`${this.apiUrl}/${propertyId}/rooms/${roomId}/images`, formData)
      .pipe(map(r => r.data));
  }

  getRoomImages(propertyId: number, roomId: number): Observable<RoomImage[]> {
    return this.http.get<ApiResponse<RoomImage[]>>(`${this.apiUrl}/${propertyId}/rooms/${roomId}/images`)
      .pipe(map(r => r.data));
  }

  deleteRoomImage(propertyId: number, roomId: number, imageId: number): Observable<string> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${propertyId}/rooms/${roomId}/images/${imageId}`)
      .pipe(map(r => r.data));
  }

  setRoomThumbnail(propertyId: number, roomId: number, imageId: number): Observable<RoomImage> {
    return this.http.patch<ApiResponse<RoomImage>>(`${this.apiUrl}/${propertyId}/rooms/${roomId}/images/${imageId}/thumbnail`, {})
      .pipe(map(r => r.data));
  }
}
