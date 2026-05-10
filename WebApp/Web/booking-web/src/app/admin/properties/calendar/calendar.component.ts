import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { InventoryService } from '../services/inventory.service';
import { PropertyService } from '../services/property.service';
import {
  DayInventory,
  HostCalendarResponse,
  RoomCalendar
} from '../models/inventory.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';
import { ToastService } from '../../../shared/services/toast.service';

interface MonthCell {
  date: string;          // ISO yyyy-MM-dd, '' if filler
  day: number;           // 1-31, 0 if filler
  inMonth: boolean;
  inRange: boolean;      // có data từ backend (trong khoảng query)
  data?: DayInventory;
  status: 'empty' | 'unmanaged' | 'blocked' | 'partial' | 'full' | 'available';
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AnimateOnScrollDirective],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.css'
})
export class CalendarComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private inventoryService = inject(InventoryService);
  private propertyService = inject(PropertyService);
  private toast = inject(ToastService);

  propertyId = 0;
  propertyName = signal('');
  isLoading = signal(true);
  calendar = signal<HostCalendarResponse | null>(null);

  // Current month being viewed
  viewYear = signal(new Date().getFullYear());
  viewMonth = signal(new Date().getMonth()); // 0-11

  // Modals
  showEditDay = signal(false);
  editTarget = signal<{ room: RoomCalendar; day: DayInventory } | null>(null);
  editAvailable = 0;
  isSavingDay = signal(false);

  showBulkEdit = signal(false);
  bulkRoom = signal<RoomCalendar | null>(null);
  bulkForm = { fromDate: '', toDate: '', availableCount: 0 };
  isSavingBulk = signal(false);

  showExtend = signal(false);
  extendRoom = signal<RoomCalendar | null>(null);
  extendForm = { untilDate: '' };
  isExtending = signal(false);

  monthLabel = computed(() => {
    const m = (this.viewMonth() + 1).toString().padStart(2, '0');
    return `${m}/${this.viewYear()}`;
  });

  ngOnInit(): void {
    this.propertyId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadProperty();
    this.loadCalendar();
  }

  // ============ Data load ============

  private loadProperty(): void {
    this.propertyService.getPropertyById(this.propertyId).subscribe({
      next: (p) => this.propertyName.set(p.name),
      error: () => {}
    });
  }

  private loadCalendar(): void {
    this.isLoading.set(true);
    const from = this.firstDayOfMonth();
    const last = this.lastDayOfMonth();
    const days = this.daysBetween(from, last) + 1; // inclusive
    const fromIso = this.toIso(from);
    this.inventoryService.getCalendar(this.propertyId, fromIso, days).subscribe({
      next: (data) => {
        this.calendar.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.toast.error(err.error?.message || 'Không thể tải lịch');
      }
    });
  }

  // ============ Month nav ============

  prevMonth(): void {
    let y = this.viewYear();
    let m = this.viewMonth() - 1;
    if (m < 0) { m = 11; y -= 1; }
    this.viewYear.set(y); this.viewMonth.set(m);
    this.loadCalendar();
  }

  nextMonth(): void {
    let y = this.viewYear();
    let m = this.viewMonth() + 1;
    if (m > 11) { m = 0; y += 1; }
    this.viewYear.set(y); this.viewMonth.set(m);
    this.loadCalendar();
  }

  // ============ Grid building (per room) ============

  buildMonthGrid(room: RoomCalendar): MonthCell[] {
    const year = this.viewYear();
    const month = this.viewMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startWeekday = (firstDay.getDay() + 6) % 7; // VN: Mon=0, Sun=6
    const daysInMonth = lastDay.getDate();

    const dataByDate = new Map<string, DayInventory>();
    for (const d of room.days) dataByDate.set(d.date, d);

    const cells: MonthCell[] = [];
    // Leading fillers
    for (let i = 0; i < startWeekday; i++) {
      cells.push({ date: '', day: 0, inMonth: false, inRange: false, status: 'empty' });
    }
    for (let day = 1; day <= daysInMonth; day++) {
      const iso = this.toIso(new Date(year, month, day));
      const data = dataByDate.get(iso);
      cells.push({
        date: iso,
        day,
        inMonth: true,
        inRange: !!data,
        data,
        status: this.deriveStatus(room, data)
      });
    }
    // Trailing fillers to complete last week
    while (cells.length % 7 !== 0) {
      cells.push({ date: '', day: 0, inMonth: false, inRange: false, status: 'empty' });
    }
    return cells;
  }

  private deriveStatus(room: RoomCalendar, data?: DayInventory): MonthCell['status'] {
    if (!data || !data.hasInventory) return 'unmanaged';
    if (data.availableCount === 0 && data.bookedCount === 0) return 'blocked';
    if (data.bookedCount >= room.quantity || data.availableCount === 0) return 'full';
    if (data.bookedCount > 0) return 'partial';
    return 'available';
  }

  // ============ Edit day modal ============

  openEditDay(room: RoomCalendar, cell: MonthCell): void {
    if (!cell.inMonth || !cell.data) return;
    this.editTarget.set({ room, day: cell.data });
    this.editAvailable = cell.data.availableCount;
    this.showEditDay.set(true);
  }

  closeEditDay(): void {
    this.showEditDay.set(false);
    this.editTarget.set(null);
  }

  setEditPreset(preset: 'open' | 'block'): void {
    const t = this.editTarget();
    if (!t) return;
    if (preset === 'open') this.editAvailable = t.room.quantity - t.day.bookedCount;
    else this.editAvailable = 0;
  }

  submitEditDay(): void {
    const t = this.editTarget();
    if (!t) return;
    this.isSavingDay.set(true);
    this.inventoryService.bulkUpdate(this.propertyId, t.room.roomId, {
      fromDate: t.day.date,
      toDate: t.day.date,
      availableCount: this.editAvailable
    }).subscribe({
      next: () => {
        this.isSavingDay.set(false);
        this.showEditDay.set(false);
        this.editTarget.set(null);
        this.toast.success('Đã cập nhật ngày');
        this.loadCalendar();
      },
      error: (err) => {
        this.isSavingDay.set(false);
        this.toast.error(err.error?.message || 'Không thể cập nhật');
      }
    });
  }

  // ============ Bulk edit modal ============

  openBulkEdit(room: RoomCalendar): void {
    this.bulkRoom.set(room);
    const today = this.toIso(new Date());
    const plus7 = this.toIso(this.addDays(new Date(), 6));
    this.bulkForm = { fromDate: today, toDate: plus7, availableCount: room.quantity };
    this.showBulkEdit.set(true);
  }

  closeBulkEdit(): void {
    this.showBulkEdit.set(false);
    this.bulkRoom.set(null);
  }

  setBulkPreset(preset: 'open' | 'block'): void {
    const r = this.bulkRoom();
    if (!r) return;
    this.bulkForm.availableCount = preset === 'open' ? r.quantity : 0;
  }

  submitBulk(): void {
    const r = this.bulkRoom();
    if (!r) return;
    if (!this.bulkForm.fromDate || !this.bulkForm.toDate) {
      this.toast.error('Vui lòng chọn khoảng ngày');
      return;
    }
    if (this.bulkForm.availableCount < 0 || this.bulkForm.availableCount > r.quantity) {
      this.toast.error(`Số phòng trống phải từ 0 đến ${r.quantity}`);
      return;
    }
    this.isSavingBulk.set(true);
    this.inventoryService.bulkUpdate(this.propertyId, r.roomId, {
      fromDate: this.bulkForm.fromDate,
      toDate: this.bulkForm.toDate,
      availableCount: this.bulkForm.availableCount
    }).subscribe({
      next: (updated) => {
        this.isSavingBulk.set(false);
        this.showBulkEdit.set(false);
        this.bulkRoom.set(null);
        this.toast.success(`Đã cập nhật ${updated.length} ngày`);
        this.loadCalendar();
      },
      error: (err) => {
        this.isSavingBulk.set(false);
        this.toast.error(err.error?.message || 'Không thể cập nhật. Có thể có ngày đã có booking vượt số phòng đặt.');
      }
    });
  }

  // ============ Extend modal ============

  openExtend(room: RoomCalendar): void {
    this.extendRoom.set(room);
    // Default: today + 90 days
    const target = this.addDays(new Date(), 90);
    this.extendForm.untilDate = this.toIso(target);
    this.showExtend.set(true);
  }

  closeExtend(): void {
    this.showExtend.set(false);
    this.extendRoom.set(null);
  }

  submitExtend(): void {
    const r = this.extendRoom();
    if (!r || !this.extendForm.untilDate) return;
    this.isExtending.set(true);
    this.inventoryService.extend(this.propertyId, r.roomId, this.extendForm.untilDate).subscribe({
      next: (res) => {
        this.isExtending.set(false);
        this.showExtend.set(false);
        this.extendRoom.set(null);
        if (res.created > 0) this.toast.success(`Đã mở thêm ${res.created} ngày`);
        else this.toast.info('Không có ngày mới nào được mở (đã đầy đủ)');
        this.loadCalendar();
      },
      error: (err) => {
        this.isExtending.set(false);
        this.toast.error(err.error?.message || 'Không thể mở lịch');
      }
    });
  }

  // ============ Date helpers ============

  private firstDayOfMonth(): Date { return new Date(this.viewYear(), this.viewMonth(), 1); }
  private lastDayOfMonth(): Date { return new Date(this.viewYear(), this.viewMonth() + 1, 0); }
  private toIso(d: Date): string {
    const y = d.getFullYear();
    const m = (d.getMonth() + 1).toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
  private addDays(d: Date, n: number): Date {
    const r = new Date(d);
    r.setDate(r.getDate() + n);
    return r;
  }
  private daysBetween(a: Date, b: Date): number {
    return Math.round((b.getTime() - a.getTime()) / 86400000);
  }
}
