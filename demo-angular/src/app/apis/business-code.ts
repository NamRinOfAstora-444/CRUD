// UserErrorCode
export enum UserErrorCode {
  USER_NOT_FOUND = 'USER_01',
  USER_ALREADY_EXISTS = 'USER_02',
  EMAIL_ALREADY_EXISTS = 'USER_03',
  INVALID_USER_DATA = 'USER_04',
}

export const UserErrorCodeMessages: Record<UserErrorCode, string> = {
  [UserErrorCode.USER_NOT_FOUND]: 'Không tìm thấy người dùng',
  [UserErrorCode.USER_ALREADY_EXISTS]: 'Người dùng đã tồn tại',
  [UserErrorCode.EMAIL_ALREADY_EXISTS]: 'Email đã được sử dụng',
  [UserErrorCode.INVALID_USER_DATA]: 'Dữ liệu người dùng không hợp lệ',
};

// CommonCode
export enum CommonCode {
  SUCCESS = '200',
  BAD_REQUEST = '400',
  UNAUTHORIZED = '401',
  FORBIDDEN = '403',
  NOT_FOUND = '404',
  CONFLICT = '409',
  VALIDATION_ERROR = '422',
  INTERNAL_ERROR = '500',
  SERVICE_UNAVAILABLE = '503',
}

export const CommonCodeMessages: Record<CommonCode, string> = {
  [CommonCode.SUCCESS]: 'Thao tác thành công',
  [CommonCode.BAD_REQUEST]: 'Yêu cầu không hợp lệ',
  [CommonCode.UNAUTHORIZED]: 'Chưa xác thực',
  [CommonCode.FORBIDDEN]: 'Không có quyền truy cập',
  [CommonCode.NOT_FOUND]: 'Không tìm thấy tài nguyên',
  [CommonCode.CONFLICT]: 'Dữ liệu bị trùng lặp',
  [CommonCode.VALIDATION_ERROR]: 'Dữ liệu không hợp lệ',
  [CommonCode.INTERNAL_ERROR]: 'Lỗi hệ thống',
  [CommonCode.SERVICE_UNAVAILABLE]: 'Dịch vụ tạm thời không khả dụng',
};

// Combined Business Code (All codes)
export const BusinessCode = {
  ...UserErrorCode,
  ...CommonCode,
} as const;

export type BusinessCode = UserErrorCode | CommonCode;

// Combined Messages
export const BusinessCodeMessages = {
  ...UserErrorCodeMessages,
  ...CommonCodeMessages,
};

/**
 * Các business code KHÔNG hiển thị toast tự động.
 * Caller tự chịu trách nhiệm xử lý (vd: hiển thị inline error trên form).
 */
export const SILENT_BUSINESS_CODES = new Set<string>([UserErrorCode.INVALID_USER_DATA]);
