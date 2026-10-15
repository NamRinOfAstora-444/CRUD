import { HttpInterceptorFn, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, map, throwError } from 'rxjs';

import { ToastService } from '../shared/services/toast.service';
import { BusinessCodeMessages, CommonCode, SILENT_BUSINESS_CODES } from './business-code';
import { IBaseResponse } from '../shared/models/interfaces';
import { SKIP_BUSINESS_ERROR_TOAST } from './http-context';

export class BusinessError extends Error {
  constructor(
    public readonly code: string,
    message: string,
  ) {
    super(message);
    this.name = 'BusinessError';
  }
}

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);
  const skipToast = req.context.get(SKIP_BUSINESS_ERROR_TOAST);

  return next(req).pipe(
    // Xử lý response HTTP 200: kiểm tra business code
    map((event) => {
      if (!(event instanceof HttpResponse)) return event;

      const body = event.body as IBaseResponse<unknown>;
      if (!body?.code || body.code === CommonCode.SUCCESS) return event;

      // Business error: code != SUCCESS
      const message =
        BusinessCodeMessages[body.code as keyof typeof BusinessCodeMessages] ??
        body.message ??
        'Đã xảy ra lỗi không xác định';

      // Business error thuộc loại bắn message sẽ bắn toast
      if (!skipToast && !SILENT_BUSINESS_CODES.has(body.code)) {
        toastService.error(message);
      }

      return event;
    }),

    // Xử lý lỗi HTTP (status != 200)
    catchError((error) => {
      if (error instanceof HttpErrorResponse && !skipToast) {
        const errorData = error.error;
        let message = 'Đã xảy ra lỗi không xác định';

        if (
          errorData?.code &&
          BusinessCodeMessages[errorData.code as keyof typeof BusinessCodeMessages]
        ) {
          message = BusinessCodeMessages[errorData.code as keyof typeof BusinessCodeMessages];
        } else if (errorData?.message) {
          message = errorData.message;
        } else if (error.message) {
          message = error.message;
        }

        toastService.error(message);
      }

      return throwError(() => error);
    }),
  );
};
