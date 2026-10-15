import { Injectable, inject, signal } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';
import { UserApiService } from '../../apis/user/user.api';
import { IUserCreateRequest, IUserResponse, IUserUpdateRequest } from '../../apis/user/user.interface';
import { ToastService } from './toast.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly userApiService = inject(UserApiService);
  private readonly toastService = inject(ToastService);

  users = signal<IUserResponse[]>([]);
  loading = signal(false);
  totalRecords = signal(0);
  currentPage = signal(0);
  currentSize = signal(10);
  error = signal<string | null>(null);
  mutationLoading = signal(false);

  fetchUsersAsync(page = this.currentPage(), size = this.currentSize()): void {
    this.loading.set(true);
    this.error.set(null);

    this.userApiService.getAllUsers(page, size).pipe(
      catchError((error: unknown) => {
        this.error.set('Không thể tải danh sách người dùng.');
        return throwError(() => error);
      }),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: (response) => {
        if (response.code !== '200') {
          this.error.set(response.message || 'Không thể tải danh sách người dùng.');
          return;
        }
        this.users.set(response.result.content);
        this.totalRecords.set(response.result.totalElements);
        this.currentPage.set(response.result.page ?? page);
        this.currentSize.set(response.result.size ?? size);
      },
      error: () => {},
    });
  }

  createUserAsync(request: IUserCreateRequest, onSuccess?: () => void): void {
    this.mutationLoading.set(true);
    this.userApiService.createUser(request).pipe(
      finalize(() => this.mutationLoading.set(false)),
    ).subscribe({
      next: (response) => {
        if (response.code !== '200') {
          this.toastService.error(response.message || 'Không thể tạo người dùng.');
          return;
        }
        this.toastService.success('Tạo tài khoản thành công.');
        onSuccess?.();
        this.fetchUsersAsync(0, this.currentSize());
      },
      error: () => this.toastService.error('Không thể tạo người dùng. Vui lòng thử lại.'),
    });
  }

  updateUserAsync(id: number, request: IUserUpdateRequest, onSuccess?: () => void): void {
    this.mutationLoading.set(true);
    this.userApiService.updateUser(id, request).pipe(
      finalize(() => this.mutationLoading.set(false)),
    ).subscribe({
      next: (response) => {
        if (response.code !== '200') {
          this.toastService.error(response.message || 'Không thể cập nhật người dùng.');
          return;
        }
        this.toastService.success('Cập nhật tài khoản thành công.');
        onSuccess?.();
        this.fetchUsersAsync(0, this.currentSize());
      },
      error: () => this.toastService.error('Không thể cập nhật người dùng. Vui lòng thử lại.'),
    });
  }

  deleteUserAsync(id: number, onSuccess?: () => void): void {
    const pageBeforeDelete = this.currentPage();
    const size = this.currentSize();
    const remainingOnPage = this.users().length - 1;
    const nextPage = pageBeforeDelete > 0 && remainingOnPage <= 0
      ? pageBeforeDelete - 1
      : pageBeforeDelete;

    this.mutationLoading.set(true);
    this.userApiService.deleteUser(id).pipe(
      finalize(() => this.mutationLoading.set(false)),
    ).subscribe({
      next: (response) => {
        if (response.code !== '200') {
          this.toastService.error(response.message || 'Không thể xóa người dùng.');
          return;
        }
        this.toastService.success('Xóa người dùng thành công.');
        onSuccess?.();
        this.fetchUsersAsync(nextPage, size);
      },
      error: () => this.toastService.error('Không thể xóa người dùng. Vui lòng thử lại.'),
    });
  }
}
