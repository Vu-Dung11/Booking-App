import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  PropertyService,
  PropertyDetailResponse,
  RoomRequest,
  PropertyRequest,
  RoomSearchResponse
} from '../services/property.service';
import { PropertyImage, RoomImage } from '../models/property.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';
import { ToastService } from '../../../shared/services/toast.service';

const EMPTY_ROOM: RoomRequest = { roomType: '', capacity: 2, basePrice: 0, quantity: 1 };

const EMPTY_PROPERTY_FORM: PropertyRequest = {
  name: '', description: '', address: '', city: '', country: 'Vietnam'
};

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
  private toast = inject(ToastService);

  propertyId = 0;
  property = signal<PropertyDetailResponse | null>(null);
  isLoading = signal(true);

  // Property edit modal
  showEditProperty = signal(false);
  propertyForm: PropertyRequest = { ...EMPTY_PROPERTY_FORM };
  isSavingProperty = signal(false);

  // Property images gallery
  propertyImages = signal<PropertyImage[]>([]);
  isLoadingImages = signal(false);
  isUploadingImages = signal(false);

  // Room modal (add or edit)
  showRoomModal = signal(false);
  roomModalMode = signal<'add' | 'edit'>('add');
  editingRoomId = signal<number | null>(null);
  roomForm: RoomRequest = { ...EMPTY_ROOM };
  isSavingRoom = signal(false);

  // Delete room confirm
  showDeleteRoomConfirm = signal(false);
  deletingRoom = signal<RoomSearchResponse | null>(null);
  isDeletingRoom = signal(false);

  // Room images modal
  showRoomImagesModal = signal(false);
  managingRoom = signal<RoomSearchResponse | null>(null);
  roomImages = signal<RoomImage[]>([]);
  isLoadingRoomImages = signal(false);
  isUploadingRoomImages = signal(false);

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
        this.loadPropertyImages();
      },
      error: () => {
        this.isLoading.set(false);
        this.toast.error('Khong tai duoc thong tin homestay.');
      }
    });
  }

  // ===================== Property info =====================

  toggleActive(): void {
    const p = this.property();
    if (!p) return;
    const obs$ = p.isActive
      ? this.propertyService.deactivateProperty(this.propertyId)
      : this.propertyService.activateProperty(this.propertyId);
    obs$.subscribe({
      next: () => {
        this.toast.success(p.isActive ? 'Da an homestay khoi tim kiem' : 'Da kich hoat lai homestay');
        this.loadDetail();
      },
      error: (err) => this.toast.error(err.error?.message || 'Khong the cap nhat trang thai')
    });
  }

  openEditProperty(): void {
    const p = this.property();
    if (!p) return;
    this.propertyForm = {
      name: p.name,
      description: p.description ?? '',
      address: p.address,
      city: p.city,
      country: p.country
    };
    this.showEditProperty.set(true);
  }

  closeEditProperty(): void {
    this.showEditProperty.set(false);
  }

  saveProperty(): void {
    if (!this.propertyForm.name || !this.propertyForm.address || !this.propertyForm.city) return;
    this.isSavingProperty.set(true);
    this.propertyService.updateProperty(this.propertyId, this.propertyForm).subscribe({
      next: () => {
        this.isSavingProperty.set(false);
        this.showEditProperty.set(false);
        this.toast.success('Da cap nhat thong tin homestay');
        this.loadDetail();
      },
      error: (err) => {
        this.isSavingProperty.set(false);
        this.toast.error(err.error?.message || 'Khong the luu thay doi');
      }
    });
  }

  // ===================== Property images =====================

  loadPropertyImages(): void {
    this.isLoadingImages.set(true);
    this.propertyService.getPropertyImages(this.propertyId).subscribe({
      next: (imgs) => {
        this.propertyImages.set(imgs);
        this.isLoadingImages.set(false);
      },
      error: () => this.isLoadingImages.set(false)
    });
  }

  onPropertyFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const files = this.validateFiles(Array.from(input.files));
    if (files.length === 0) {
      input.value = '';
      return;
    }
    this.uploadPropertyImages(files);
    input.value = '';
  }

  private uploadPropertyImages(files: File[]): void {
    this.isUploadingImages.set(true);
    this.propertyService.uploadPropertyImages(this.propertyId, files).subscribe({
      next: () => {
        this.isUploadingImages.set(false);
        this.toast.success(`Da upload ${files.length} anh`);
        this.loadPropertyImages();
        this.loadDetail();
      },
      error: (err) => {
        this.isUploadingImages.set(false);
        this.toast.error(err.error?.message || 'Upload that bai');
      }
    });
  }

  setPropertyThumbnail(image: PropertyImage): void {
    if (image.isThumbnail) return;
    this.propertyService.setPropertyThumbnail(this.propertyId, image.id).subscribe({
      next: () => {
        this.toast.success('Da dat lam anh dai dien');
        this.loadPropertyImages();
        this.loadDetail();
      },
      error: (err) => this.toast.error(err.error?.message || 'Khong the doi anh dai dien')
    });
  }

  deletePropertyImage(image: PropertyImage): void {
    if (!confirm('Xoa anh nay?')) return;
    this.propertyService.deletePropertyImage(this.propertyId, image.id).subscribe({
      next: () => {
        this.toast.success('Da xoa anh');
        this.loadPropertyImages();
        this.loadDetail();
      },
      error: (err) => this.toast.error(err.error?.message || 'Khong the xoa anh')
    });
  }

  // ===================== Room CRUD =====================

  openAddRoom(): void {
    this.roomModalMode.set('add');
    this.editingRoomId.set(null);
    this.roomForm = { ...EMPTY_ROOM };
    this.showRoomModal.set(true);
  }

  openEditRoom(room: RoomSearchResponse): void {
    this.roomModalMode.set('edit');
    this.editingRoomId.set(room.roomId);
    this.roomForm = {
      roomType: room.roomType,
      capacity: room.capacity,
      basePrice: room.price,
      quantity: room.quantity ?? 1
    };
    this.showRoomModal.set(true);
  }

  closeRoomModal(): void {
    this.showRoomModal.set(false);
  }

  saveRoom(): void {
    if (!this.roomForm.roomType || this.roomForm.basePrice <= 0) return;
    this.isSavingRoom.set(true);
    const obs$ = this.roomModalMode() === 'edit' && this.editingRoomId() != null
      ? this.propertyService.updateRoom(this.propertyId, this.editingRoomId()!, this.roomForm)
      : this.propertyService.addRoom(this.propertyId, this.roomForm);

    obs$.subscribe({
      next: () => {
        this.isSavingRoom.set(false);
        this.showRoomModal.set(false);
        this.toast.success(this.roomModalMode() === 'edit' ? 'Da cap nhat phong' : 'Da them phong');
        this.loadDetail();
      },
      error: (err) => {
        this.isSavingRoom.set(false);
        this.toast.error(err.error?.message || 'Khong the luu phong');
      }
    });
  }

  openDeleteRoom(room: RoomSearchResponse): void {
    this.deletingRoom.set(room);
    this.showDeleteRoomConfirm.set(true);
  }

  closeDeleteRoom(): void {
    this.showDeleteRoomConfirm.set(false);
    this.deletingRoom.set(null);
  }

  confirmDeleteRoom(): void {
    const room = this.deletingRoom();
    if (!room) return;
    this.isDeletingRoom.set(true);
    this.propertyService.deleteRoom(this.propertyId, room.roomId).subscribe({
      next: () => {
        this.isDeletingRoom.set(false);
        this.showDeleteRoomConfirm.set(false);
        this.deletingRoom.set(null);
        this.toast.success('Da xoa phong');
        this.loadDetail();
      },
      error: (err) => {
        this.isDeletingRoom.set(false);
        this.toast.error(err.error?.message || 'Khong the xoa phong');
      }
    });
  }

  // ===================== Room images =====================

  openRoomImagesModal(room: RoomSearchResponse): void {
    this.managingRoom.set(room);
    this.showRoomImagesModal.set(true);
    this.loadRoomImages(room.roomId);
  }

  closeRoomImagesModal(): void {
    this.showRoomImagesModal.set(false);
    this.managingRoom.set(null);
    this.roomImages.set([]);
  }

  loadRoomImages(roomId: number): void {
    this.isLoadingRoomImages.set(true);
    this.propertyService.getRoomImages(this.propertyId, roomId).subscribe({
      next: (imgs) => {
        this.roomImages.set(imgs);
        this.isLoadingRoomImages.set(false);
      },
      error: () => this.isLoadingRoomImages.set(false)
    });
  }

  onRoomFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const room = this.managingRoom();
    if (!input.files || input.files.length === 0 || !room) return;
    const files = this.validateFiles(Array.from(input.files));
    if (files.length === 0) {
      input.value = '';
      return;
    }
    this.isUploadingRoomImages.set(true);
    this.propertyService.uploadRoomImages(this.propertyId, room.roomId, files).subscribe({
      next: () => {
        this.isUploadingRoomImages.set(false);
        this.toast.success(`Da upload ${files.length} anh phong`);
        this.loadRoomImages(room.roomId);
        this.loadDetail();
      },
      error: (err) => {
        this.isUploadingRoomImages.set(false);
        this.toast.error(err.error?.message || 'Upload that bai');
      }
    });
    input.value = '';
  }

  setRoomThumbnail(image: RoomImage): void {
    const room = this.managingRoom();
    if (!room || image.isThumbnail) return;
    this.propertyService.setRoomThumbnail(this.propertyId, room.roomId, image.id).subscribe({
      next: () => {
        this.toast.success('Da dat lam anh dai dien phong');
        this.loadRoomImages(room.roomId);
        this.loadDetail();
      },
      error: (err) => this.toast.error(err.error?.message || 'Khong the doi anh dai dien')
    });
  }

  deleteRoomImage(image: RoomImage): void {
    const room = this.managingRoom();
    if (!room || !confirm('Xoa anh nay?')) return;
    this.propertyService.deleteRoomImage(this.propertyId, room.roomId, image.id).subscribe({
      next: () => {
        this.toast.success('Da xoa anh');
        this.loadRoomImages(room.roomId);
        this.loadDetail();
      },
      error: (err) => this.toast.error(err.error?.message || 'Khong the xoa anh')
    });
  }

  // ===================== helpers =====================

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price || 0);
  }

  /** Client-side validate: max 10MB, image only. */
  private validateFiles(files: File[]): File[] {
    const valid: File[] = [];
    for (const f of files) {
      if (!f.type.startsWith('image/')) {
        this.toast.error(`${f.name}: chi nhan file anh`);
        continue;
      }
      if (f.size > 10 * 1024 * 1024) {
        this.toast.error(`${f.name}: vuot qua 10MB`);
        continue;
      }
      valid.push(f);
    }
    return valid;
  }
}
