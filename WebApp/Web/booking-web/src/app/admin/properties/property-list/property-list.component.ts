import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PropertyService } from '../services/property.service';
import { Property } from '../models/property.model';
import { Page } from '../../../shared/models/api-response.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';

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

  showCreateModal = signal(false);
  createForm = { name: '', description: '', address: '', city: '', country: 'Vietnam' };
  isCreating = signal(false);

  ngOnInit(): void {
    this.loadProperties();
  }

  loadProperties(): void {
    this.isLoading.set(true);
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

  openCreateModal(): void {
    this.createForm = { name: '', description: '', address: '', city: '', country: 'Vietnam' };
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  submitCreate(): void {
    if (!this.createForm.name || !this.createForm.address || !this.createForm.city) return;
    this.isCreating.set(true);
    this.propertyService.createProperty(this.createForm).subscribe({
      next: () => {
        this.isCreating.set(false);
        this.showCreateModal.set(false);
        this.loadProperties();
      },
      error: () => this.isCreating.set(false)
    });
  }
}
