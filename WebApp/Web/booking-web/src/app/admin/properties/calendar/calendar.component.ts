import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { InventoryService } from '../services/inventory.service';
import { PropertyService } from '../services/property.service';
import {
  BookingByDateItem,
  DayInventory,
  HostCalendarResponse,
  RoomCalendar
} from '../models/inventory.model';
import { AnimateOnScrollDirective } from '../../../shared/directives/animate-on-scroll.directive';
import { ToastService } from '../../../shared/services/toast.service';

type CellStatus =
  | 'empty'
  | 'unmanaged'
  | 'blocked'
  | 'blocked-with-booking'
  | 'partial'
  | 'full'
  | 'limited'
  | 'available';

interface MonthCell {
  date: string;          // ISO yyyy-MM-dd, '' if filler
  day: number;           // 1-31, 0 if filler
  inMonth: boolean;
  inRange: boolean;      // có data từ backend (trong khoảng query)
  data?: DayInventory;
  status: CellStatus;
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
  propertyActive = signal(true);
  isLoading = signal(true);
  calendar = signal<HostCalendarResponse | null>(null);

  // Drill-down: bookings của ngày đang xem trong Edit Day modal
  dayBookings = signal<BookingByDateItem[]>([]);
  isLoadingDayBookings = signal(false);

  // Bulk edit preflight: max availableCount cho range hiện tại
  bulkMaxAvailable = signal<number | null>(null);

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
      next: (p) => {
        this.propertyName.set(p.name);
        this.propertyActive.set(p.isActive !== false);
      },
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

  private deriveStatus(room: RoomCalendar, data?: DayInventory): CellStatus {
    if (!data || !data.hasInventory) return 'unmanaged';

    // Host đã khoá ngày (chủ động đặt available=0)
    if (data.availableCount === 0) {
      return data.bookedCount > 0 ? 'blocked-with-booking' : 'blocked';
    }

    // Đã đầy: booked đã đạt giới hạn availableCount mà host cho phép
    // (không so sánh với quantity — host có thể chỉ mở 3/10 phòng)
    if (data.bookedCount >= data.availableCount) return 'full';

    // Có khách nhưng chưa đầy
    if (data.bookedCount > 0) return 'partial';

    // Chưa có ai đặt, nhưng host giới hạn số phòng < số vật lý
    if (data.availableCount < room.quantity) return 'limited';

    return 'available';
  }

  // ============ Edit day modal ============

  openEditDay(room: RoomCalendar, cell: MonthCell): void {
    if (!cell.inMonth) return;
    // FIX #2: ngày chưa mở lịch → toast hint thay vì silent no-op
    if (!cell.data || !cell.data.hasInventory) {
      this.toast.info('Ngày này chưa có lịch. Bấm "Mở lịch đến..." để mở.');
      return;
    }
    this.editTarget.set({ room, day: cell.data });
    this.editAvailable = cell.data.availableCount;
    this.showEditDay.set(true);
    // FIX #6: load bookings của ngày này để host xem ai đang giữ chỗ
    this.dayBookings.set([]);
    if (cell.data.bookedCount > 0) {
      this.isLoadingDayBookings.set(true);
      this.inventoryService.getBookingsForDay(this.propertyId, room.roomId, cell.data.date).subscribe({
        next: (list) => {
          this.dayBookings.set(list);
          this.isLoadingDayBookings.set(false);
        },
        error: () => this.isLoadingDayBookings.set(false)
      });
    }
  }

  closeEditDay(): void {
    this.showEditDay.set(false);
    this.editTarget.set(null);
  }

  setEditPreset(preset: 'open' | 'block'): void {
    const t = this.editTarget();
    if (!t) return;
    // "Mở hết" = mở toàn bộ số phòng vật lý (host cap = quantity)
    // bookedCount được tính riêng bởi booking system, không trừ ở đây
    if (preset === 'open') this.editAvailable = t.room.quantity;
    else this.editAvailable = 0;
  }

  submitEditDay(): void {
    const t = this.editTarget();
    if (!t) return;
    const available = Number(this.editAvailable);
    if (isNaN(available) || available < 0 || available > t.room.quantity) {
      this.toast.error(`Số phòng trống phải từ 0 đến ${t.room.quantity}`);
      return;
    }
    this.isSavingDay.set(true);
    this.inventoryService.bulkUpdate(this.propertyId, t.room.roomId, {
      fromDate: t.day.date,
      toDate: t.day.date,
      availableCount: available
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
    this.recomputeBulkMax();
  }

  /**
   * FIX #3: tính maxAvailable host được phép set cho range hiện tại.
   * = quantity - max(bookedCount across days in range).
   * Dùng dữ liệu calendar đã load để tránh round-trip API.
   * Nếu range vượt khỏi calendar đang load (vd qua tháng khác), trả null
   * để hiển thị fallback hint.
   */
  recomputeBulkMax(): void {
    const room = this.bulkRoom();
    if (!room || !this.bulkForm.fromDate || !this.bulkForm.toDate) {
      this.bulkMaxAvailable.set(null);
      return;
    }
    const from = this.bulkForm.fromDate;
    const to = this.bulkForm.toDate;
    if (from > to) {
      this.bulkMaxAvailable.set(null);
      return;
    }
    let maxBooked = 0;
    let coveredAll = true;
    // Dùng string comparison để tránh timezone shift khi new Date()
    let cursor = from;
    while (cursor <= to) {
      const day = room.days.find(x => x.date === cursor);
      if (!day) {
        coveredAll = false;
      } else if (day.bookedCount > maxBooked) {
        maxBooked = day.bookedCount;
      }
      // Tăng cursor 1 ngày bằng Date ở local time
      const next = new Date(cursor + 'T00:00:00');
      next.setDate(next.getDate() + 1);
      cursor = this.toIso(next);
    }
    if (!coveredAll) {
      this.bulkMaxAvailable.set(null);   // không đủ data, để BE validate
      return;
    }
    this.bulkMaxAvailable.set(room.quantity - maxBooked);
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
    const available = Number(this.bulkForm.availableCount);
    if (isNaN(available) || available < 0 || available > r.quantity) {
      this.toast.error(`Số phòng trống phải từ 0 đến ${r.quantity}`);
      return;
    }
    this.isSavingBulk.set(true);
    this.inventoryService.bulkUpdate(this.propertyId, r.roomId, {
      fromDate: this.bulkForm.fromDate,
      toDate: this.bulkForm.toDate,
      availableCount: available
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

  // ============ Presentation helpers (label + today + tooltip) ============

  private readonly statusLabels: Record<CellStatus, string> = {
    available: 'Còn trống',
    limited: 'Mở giới hạn',
    partial: 'Có khách',
    full: 'Đã đầy',
    blocked: 'Đã khoá',
    'blocked-with-booking': 'Khoá có khách',
    unmanaged: 'Chưa mở',
    empty: ''
  };

  /** Label tiếng Việt cho mỗi trạng thái cell. */
  getStatusLabel(status: CellStatus): string {
    return this.statusLabels[status] || '';
  }

  /** Cell có phải là ngày hôm nay không. */
  isToday(cell: MonthCell): boolean {
    if (!cell.inMonth || !cell.date) return false;
    return cell.date === this.toIso(new Date());
  }

  /**
   * Cell có phải thứ 7 hoặc CN không.
   * Dùng trực tiếp từ date string để tránh sai vị trí filler.
   */
  isWeekend(index: number): boolean {
    // index là vị trí trong grid 7-cột (0=T2 ... 5=T7, 6=CN)
    const dow = index % 7;
    return dow === 5 || dow === 6;   // T7 (5), CN (6)
  }

  /** Trả về ngày trong tuần (VN) của một cell inMonth để tô màu cuối tuần đúng. */
  getCellWeekday(cell: MonthCell): number {
    if (!cell.date) return -1;
    const d = new Date(cell.date + 'T00:00:00'); // force local
    return (d.getDay() + 6) % 7; // Mon=0, Sun=6
  }

  /** DD/MM/YYYY từ ISO date. */
  formatDateVi(iso: string): string {
    if (!iso) return '';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  }

  /** Tooltip chi tiết khi hover. */
  buildCellTooltip(room: RoomCalendar, cell: MonthCell): string {
    if (!cell.inMonth || !cell.date) return '';
    const dateStr = this.formatDateVi(cell.date);
    if (!cell.data || !cell.data.hasInventory) {
      return `${dateStr} — chưa mở lịch. Bấm "Mở lịch đến..." để mở.`;
    }
    const label = this.getStatusLabel(cell.status);
    const todayMarker = this.isToday(cell) ? ' (Hôm nay)' : '';
    // Phòng có thể đặt thêm = availableCount (cap host đặt) − bookedCount (đã giữ chỗ)
    const remainingBookable = Math.max(0, cell.data.availableCount - cell.data.bookedCount);
    return `${dateStr}${todayMarker} — ${label}. ` +
      `${remainingBookable} phòng có thể đặt thêm ` +
      `(giới hạn: ${cell.data.availableCount}, đã giữ: ${cell.data.bookedCount} / ${room.quantity} phòng vật lý).`;
  }
}
