import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PropertyRequest, PropertyService } from '../services/property.service';
import { Property } from '../models/property.model';
import { Page } from '../../../shared/models/api-response.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

type ModalMode = 'create' | 'edit';

const EMPTY_FORM: PropertyRequest = {
  name: '',
  description: '',
  address: '',
  city: '',
  country: 'Vietnam'
};

@Component({
  selector: 'app-property-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './property-list.component.html',
  styleUrl: './property-list.component.css'
})
export class PropertyListComponent implements OnInit {
  private propertyService = inject(PropertyService);

  properties = signal<Property[]>([]);
  isLoading = signal(true);
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  pageSize = 9;
  searchTerm = '';
  errorMessage = signal('');

  // Modal create / edit
  showFormModal = signal(false);
  modalMode = signal<ModalMode>('create');
  editingId = signal<number | null>(null);
  formData: PropertyRequest = { ...EMPTY_FORM };
  isSaving = signal(false);

  // Modal delete confirm
  showDeleteModal = signal(false);
  deletingProperty = signal<Property | null>(null);
  isDeleting = signal(false);

  ngOnInit(): void {
    this.loadProperties();
  }

  loadProperties(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');
    this.propertyService.getProperties(this.currentPage(), this.pageSize).subscribe({
      next: (page: Page<Property>) => {
        this.properties.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  get filteredProperties(): Property[] {
    if (!this.searchTerm) return this.properties();
    const term = this.searchTerm.toLowerCase();
    return this.properties().filter(p =>
      p.name.toLowerCase().includes(term) ||
      p.city.toLowerCase().includes(term)
    );
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.loadProperties();
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  // ===== Create / Edit =====
  openCreateModal(): void {
    this.modalMode.set('create');
    this.editingId.set(null);
    this.formData = { ...EMPTY_FORM };
    this.errorMessage.set('');
    this.showFormModal.set(true);
  }

  openEditModal(property: Property): void {
    this.modalMode.set('edit');
    this.editingId.set(property.id);
    this.formData = {
      name: property.name,
      description: property.description ?? '',
      address: property.address,
      city: property.city,
      country: property.country
    };
    this.errorMessage.set('');
    this.showFormModal.set(true);
  }

  closeFormModal(): void {
    this.showFormModal.set(false);
  }

  submitForm(): void {
    if (!this.formData.name || !this.formData.address || !this.formData.city) return;
    this.isSaving.set(true);
    this.errorMessage.set('');

    const obs$ = this.modalMode() === 'edit' && this.editingId() != null
      ? this.propertyService.updateProperty(this.editingId()!, this.formData)
      : this.propertyService.createProperty(this.formData);

    obs$.subscribe({
      next: () => {
        this.isSaving.set(false);
        this.showFormModal.set(false);
        this.loadProperties();
      },
      error: (err) => {
        this.isSaving.set(false);
        this.errorMessage.set(err.error?.message || 'Khong the luu thay doi. Vui long thu lai.');
      }
    });
  }

  // ===== Soft toggle (deactivate / activate) =====
  toggleActive(property: Property): void {
    const obs$ = property.isActive
      ? this.propertyService.deactivateProperty(property.id)
      : this.propertyService.activateProperty(property.id);
    obs$.subscribe({
      next: () => this.loadProperties()
    });
  }

  // ===== Hard delete =====
  openDeleteModal(property: Property): void {
    this.deletingProperty.set(property);
    this.errorMessage.set('');
    this.showDeleteModal.set(true);
  }

  closeDeleteModal(): void {
    this.showDeleteModal.set(false);
    this.deletingProperty.set(null);
  }

  confirmHardDelete(): void {
    const target = this.deletingProperty();
    if (!target) return;
    this.isDeleting.set(true);
    this.errorMessage.set('');
    this.propertyService.deleteProperty(target.id).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.showDeleteModal.set(false);
        this.deletingProperty.set(null);
        this.loadProperties();
      },
      error: (err) => {
        this.isDeleting.set(false);
        this.errorMessage.set(err.error?.message || 'Khong the xoa vinh vien (con rang buoc du lieu). Hay dung "An homestay".');
      }
    });
  }
}
