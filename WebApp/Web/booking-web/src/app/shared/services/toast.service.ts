import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info';

export interface Toast {
  id: number;
  type: ToastType;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  toasts = signal<Toast[]>([]);

  show(type: ToastType, message: string, durationMs = 3000): void {
    const id = this.nextId++;
    this.toasts.update(arr => [...arr, { id, type, message }]);
    setTimeout(() => this.dismiss(id), durationMs);
  }

  success(msg: string, durationMs?: number): void { this.show('success', msg, durationMs); }
  error(msg: string, durationMs?: number): void { this.show('error', msg, durationMs); }
  info(msg: string, durationMs?: number): void { this.show('info', msg, durationMs); }

  dismiss(id: number): void {
    this.toasts.update(arr => arr.filter(t => t.id !== id));
  }
}
