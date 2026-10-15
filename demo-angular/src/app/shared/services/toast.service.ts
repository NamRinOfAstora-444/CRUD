import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ToastMessage {
  severity: 'success' | 'error' | 'info' | 'warn';
  summary: string;
  detail: string;
  life?: number;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private toastSubject = new Subject<ToastMessage>();
  public toast$ = this.toastSubject.asObservable();

  success(message: string, summary: string = 'Thành công') {
    this.toastSubject.next({
      severity: 'success',
      summary,
      detail: message,
      life: 3000,
    });
  }

  error(message: string, summary: string = 'Thông báo') {
    this.toastSubject.next({
      severity: 'error',
      summary,
      detail: message,
      life: 3000,
    });
  }

  info(message: string, summary: string = 'Thông tin') {
    this.toastSubject.next({
      severity: 'info',
      summary,
      detail: message,
      life: 3000,
    });
  }

  warn(message: string, summary: string = 'Cảnh báo') {
    this.toastSubject.next({
      severity: 'warn',
      summary,
      detail: message,
      life: 3000,
    });
  }
}
