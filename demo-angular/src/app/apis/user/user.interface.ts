export type UserRole = 'ADMIN' | 'MANAGER' | 'USER';
export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface IUserResponse {
  id: number;
  name: string;
  email: string;
  role: UserRole | string;
  status: UserStatus | string;
  /** The current project and table use updateAt. */
  updateAt: string;
}

export interface IUserCreateRequest {
  name: string;
  email: string;
  role: UserRole;
  status: UserStatus;
}

export type IUserUpdateRequest = IUserCreateRequest;
