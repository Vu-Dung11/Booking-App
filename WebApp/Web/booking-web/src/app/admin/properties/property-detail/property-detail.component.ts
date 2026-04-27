import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PropertyService, PropertyDetailResponse, RoomRequest } from '../services/property.service';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

@Component({
  selector: 'app-property-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './property-detail.component.html',
  styleUrl: './property-detail.component.css'
})
export class PropertyDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private propertyService = inject(PropertyService);

  propertyId = 0;
  property = signal<PropertyDetailResponse | null>(null);
  isLoading = signal(true);

  showAddRoom = signal(false);
  roomForm: RoomRequest = { roomType: '', capacity: 2, basePrice: 0, quantity: 1 };
  isAddingRoom = signal(false);

  ngOnInit(): void {
    this.propertyId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadDetail();
  }

  loadDetail(): void {
    this.isLoading.set(true);
    this.propertyService.getPropertyDetail(this.propertyId).subscribe({
      next: (data) => {
        this.property.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  openAddRoom(): void {
    this.roomForm = { roomType: '', capacity: 2, basePrice: 0, quantity: 1 };
    this.showAddRoom.set(true);
  }

  submitRoom(): void {
    if (!this.roomForm.roomType || this.roomForm.basePrice <= 0) return;
    this.isAddingRoom.set(true);
    this.propertyService.addRoom(this.propertyId, this.roomForm).subscribe({
      next: () => {
        this.isAddingRoom.set(false);
        this.showAddRoom.set(false);
        this.loadDetail();
      },
      error: () => this.isAddingRoom.set(false)
    });
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  }
}
