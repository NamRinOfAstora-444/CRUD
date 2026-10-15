import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { DataTableColumnDirective } from '../../shared/components/table/data-table-column.directive';
import { DataTableComponent } from '../../shared/components/table/data-table.component';
import { DataTableColumn, PageEvent } from '../../shared/components/table/data-table.interface';
import { IUserCreateRequest, IUserResponse, IUserUpdateRequest, UserRole, UserStatus } from '../../apis/user/user.interface';
import { UserService } from '../../shared/services/user.service';
import { formatDate } from '../../shared/utils/date-formatter';

type DialogMode = 'create' | 'update';
interface IUserForm {
  name: string;
  email: string;
  role: UserRole | '';
  status: UserStatus | '';
}

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    ProgressSpinnerModule,
    DataTableComponent,
    DataTableColumnDirective,
    TagModule,
  ],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css',
})
export class HomePageComponent implements OnInit {
  protected readonly userStateService = inject(UserService);
  protected readonly roleOptions = [
    { label: 'Admin', value: 'ADMIN' },
    { label: 'Manager', value: 'MANAGER' },
    { label: 'User', value: 'USER' },
  ];
  protected readonly statusOptions = [
    { label: 'Hoạt động', value: 'ACTIVE' },
    { label: 'Ngoại tuyến', value: 'INACTIVE' },
  ];

  showUserDialog = signal(false);
  showDeleteDialog = signal(false);
  dialogMode: DialogMode = 'create';
  selectedUser: IUserResponse | null = null;
  form: IUserForm = this.emptyForm();
  submitted = false;

  tableColumn: DataTableColumn[] = [
    { field: 'id', header: 'ID' },
    { field: 'name', header: 'Tên' },
    { field: 'email', header: 'Email' },
    { field: 'role', header: 'Vai trò' },
    { field: 'status', header: 'Trạng thái' },
    { field: 'updateAt', header: 'Thời gian cập nhật' },
    { field: 'actions', header: 'Thao tác' },
  ];

  ngOnInit(): void {
    this.userStateService.fetchUsersAsync(0, 10);
  }

  onPageChange(event: PageEvent): void {
    if (
      event.page === this.userStateService.currentPage() &&
      event.rows === this.userStateService.currentSize()
    ) return;
    this.userStateService.fetchUsersAsync(event.page, event.rows);
  }

  formatDate(dateString: string): string {
    return dateString ? formatDate(dateString) : '—';
  }

  openCreateUserDialog(): void {
    this.dialogMode = 'create';
    this.selectedUser = null;
    this.resetForm();
    this.showUserDialog.set(true);
  }

  openUpdateUserDialog(user: IUserResponse): void {
    this.dialogMode = 'update';
    this.selectedUser = user;
    this.form = {
      name: user.name,
      email: user.email,
      role: this.isRole(user.role) ? user.role : '',
      status: this.isStatus(user.status) ? user.status : '',
    };
    this.submitted = false;
    this.showUserDialog.set(true);
  }

  openDeleteUserDialog(user: IUserResponse): void {
    this.selectedUser = user;
    this.showDeleteDialog.set(true);
  }

  closeUserDialog(): void {
    if (this.userStateService.mutationLoading()) return;
    this.showUserDialog.set(false);
    this.resetForm();
  }

  closeDeleteDialog(): void {
    if (this.userStateService.mutationLoading()) return;
    this.showDeleteDialog.set(false);
    this.selectedUser = null;
  }

  handleSubmit(): void {
    this.submitted = true;
    if (!this.isFormValid()) return;

    const request: IUserCreateRequest = {
      name: this.form.name.trim(),
      email: this.form.email.trim(),
      role: this.form.role as UserRole,
      status: this.form.status as UserStatus,
    };

    const onSuccess = () => {
      this.showUserDialog.set(false);
      this.resetForm();
    };

    if (this.dialogMode === 'update' && this.selectedUser) {
      this.userStateService.updateUserAsync(
        this.selectedUser.id,
        request as IUserUpdateRequest,
        onSuccess,
      );
    } else {
      this.userStateService.createUserAsync(request, onSuccess);
    }
  }

  confirmDelete(): void {
    if (!this.selectedUser) return;
    this.userStateService.deleteUserAsync(this.selectedUser.id, () => {
      this.showDeleteDialog.set(false);
      this.selectedUser = null;
    });
  }

  get nameError(): string {
    const value = this.form.name.trim();
    if (!value) return 'Vui lòng nhập tên.';
    if (value.length > 50) return 'Tên không được vượt quá 50 ký tự.';
    if (!/^\p{L}+(?:[ '\u2019-]\p{L}+)*$/u.test(value)) {
      return 'Tên chỉ được chứa chữ cái và khoảng trắng.';
    }
    return '';
  }

  get emailError(): string {
    const value = this.form.email.trim();
    if (!value) return 'Vui lòng nhập email.';
    if (value.length > 50) return 'Email không được vượt quá 50 ký tự.';
    if (!/^[A-Za-z0-9._%+-]+@gmail\.com$/.test(value)) return 'Email phải có định dạng @gmail.com.';
    return '';
  }

  get roleError(): string {
    return this.form.role ? '' : 'Vui lòng chọn vai trò.';
  }

  get statusError(): string {
    return this.form.status ? '' : 'Vui lòng chọn trạng thái.';
  }

  private isFormValid(): boolean {
    return !this.nameError && !this.emailError && !this.roleError && !this.statusError;
  }

  private resetForm(): void {
    this.form = this.emptyForm();
    this.submitted = false;
  }

  private emptyForm(): IUserForm {
    return { name: '', email: '', role: '', status: '' };
  }

  private isRole(value: string): value is UserRole {
    return value === 'ADMIN' || value === 'MANAGER' || value === 'USER';
  }

  private isStatus(value: string): value is UserStatus {
    return value === 'ACTIVE' || value === 'INACTIVE';
  }
}
