import { HttpContextToken } from '@angular/common/http';

/**
 * Khi set = true, interceptor sẽ KHÔNG hiển thị toast khi có business error (code != SUCCESS).
 * Dùng khi caller tự muốn xử lý lỗi.
 *
 * @example
 * this.http.get('/api/...', {
 *   context: new HttpContext().set(SKIP_BUSINESS_ERROR_TOAST, true)
 * })
 */
export const SKIP_BUSINESS_ERROR_TOAST = new HttpContextToken<boolean>(() => false);
