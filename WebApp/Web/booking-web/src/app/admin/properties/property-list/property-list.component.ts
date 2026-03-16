import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PropertyService } from '../services/property.service';
import { Property } from '../models/property.model';

@Component({
  selector: 'app-property-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './property-list.component.html',
  styleUrl: './property-list.component.css'
})
export class PropertyListComponent implements OnInit {
  properties: Property[] = [];
  isLoading = true;

  constructor(private propertyService: PropertyService) {}

  ngOnInit(): void {
    this.loadProperties();
  }

  loadProperties(): void {
    this.propertyService.getProperties().subscribe({
      next: (data) => {
        this.properties = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading properties', err);
        this.isLoading = false;
      }
    });
  }
}
