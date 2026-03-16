import { Property } from '../../properties/models/property.model';

export interface Room {
  id: number;
  property: Property;
  roomType: string;
  capacity: number;
  basePrice: number;
  quantity: number;
}
