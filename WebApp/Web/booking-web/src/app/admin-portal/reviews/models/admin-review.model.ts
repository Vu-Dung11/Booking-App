export interface AdminReview {
  id: number;
  rating: number;
  comment?: string;
  createdAt: string;
  bookingId: number;
  propertyId: number;
  propertyName: string;
  guestId: number;
  guestEmail: string;
  guestFullName: string;
}

export interface AdminReviewDetail {
  id: number;
  rating: number;
  comment?: string;
  createdAt: string;
  bookingId: number;
  checkInDate: string;
  checkOutDate: string;
  totalPrice: number;
  propertyId: number;
  propertyName: string;
  propertyAddress: string;
  propertyCity: string;
  guestId: number;
  guestEmail: string;
  guestFullName: string;
  guestPhone?: string;
}
