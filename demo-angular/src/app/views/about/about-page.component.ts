import { Component, signal, DestroyRef, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { Select } from 'primeng/select';
import { tap } from 'rxjs';
import { ToastService } from '../../shared/services/toast.service';
import { BusinessCode } from '../../apis/business-code';
import { untilDestroyed } from '../../shared/utils/takeUntilDestroy';
import { ROLE_OPTIONS, STATUS_OPTIONS } from './models/constants';
import { FormData, FormErrors, ICascadeSelect } from './models/interfaces';

@Component({
  selector: 'app-about-home',
  standalone: true,
  imports: [FormsModule, InputTextModule, ButtonModule, Select],
  templateUrl: './about-page.component.html',
  styleUrl: './about-page.component.css',
})
export class AboutPageComponent {
  // protected userStateService = inject(UserService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  formData = signal<FormData>({
    name: '',
    email: '',
    role: '',
    status: STATUS_OPTIONS[0].value,
  });

  errors = signal<FormErrors>({});
  untilDestroyed = untilDestroyed();

  roleOptions: ICascadeSelect[] = ROLE_OPTIONS;
  statusOptions: ICascadeSelect[] = STATUS_OPTIONS;

  constructor() {}

  updateFormField(field: keyof FormData, value: string): void {
    this.formData.update((current) => ({
      ...current,
      [field]: value,
    }));
  }

  validateForm(): boolean {
    const newErrors: FormErrors = {};
    let isValid = true;
    const currentForm = this.formData();

    if (!currentForm.name.trim()) {
      newErrors.name = 'Tên không được để trống';
      isValid = false;
    }

    if (!currentForm.email.trim()) {
      newErrors.email = 'Email không được để trống';
      isValid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(currentForm.email)) {
      newErrors.email = 'Email không hợp lệ';
      isValid = false;
    }

    if (!currentForm.role) {
      newErrors.role = 'Vai trò không được để trống';
      isValid = false;
    }

    if (!currentForm.status) {
      newErrors.status = 'Trạng thái không được để trống';
      isValid = false;
    }

    this.errors.set(newErrors);
    return isValid;
  }

  // handleSubmit(): void {
  //   if (!this.validateForm()) {
  //     return;
  //   }
  //
  //   this.userStateService
  //     .createUserAsync(this.formData())
  //     .pipe(
  //       tap((res) => {
  //         if (res.code === BusinessCode.SUCCESS) {
  //           this.toastService.success('Tạo tài khoản thành công!');
  //           void this.router.navigate(['/']);
  //         }
  //       }),
  //       this.untilDestroyed(),
  //     )
  //     .subscribe();
  // }

  handleCancel(): void {
    void this.router.navigate(['/']);
  }
}
