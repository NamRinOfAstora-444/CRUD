import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { IBasePagination, IBaseResponse } from '../../shared/models/interfaces';
import { IUserCreateRequest, IUserResponse, IUserUpdateRequest } from './user.interface';
import { SKIP_BUSINESS_ERROR_TOAST } from '../http-context';

interface IUserBackendResponse {
  id: number;
  name: string;
  email: string;
  role: string;
  status: string;
  updatedAt?: string;
  updateAt?: string;
}
interface IBackendPage {
  content: IUserBackendResponse[];
  totalElements: number;
  page: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);
  private readonly API_BASE_URL = 'http://localhost:8080/api/users';

  getAllUsers(page: number, size: number): Observable<IBaseResponse<IBasePagination<IUserResponse>>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<IBaseResponse<IBackendPage>>(this.API_BASE_URL, { params }).pipe(
      map((response) => {
        if (response.code !== '200' || !response.result) {
          return response as unknown as IBaseResponse<IBasePagination<IUserResponse>>;
        }
        return {
          ...response,
          result: {
            ...response.result,
            content: response.result.content.map((user) => this.mapUser(user)),
          },
        };
      }),
    );
  }

  createUser(request: IUserCreateRequest): Observable<IBaseResponse<IUserResponse>> {
    return this.http.post<IBaseResponse<IUserBackendResponse>>(this.API_BASE_URL, request, { context: this.skipToastContext() }).pipe(
      map((response) => response.code !== '200' || !response.result
        ? response as unknown as IBaseResponse<IUserResponse>
        : { ...response, result: this.mapUser(response.result) }),
    );
  }

  updateUser(id: number, request: IUserUpdateRequest): Observable<IBaseResponse<IUserResponse>> {
    return this.http.put<IBaseResponse<IUserBackendResponse>>(`${this.API_BASE_URL}/${id}`, request, { context: this.skipToastContext() }).pipe(
      map((response) => response.code !== '200' || !response.result
        ? response as unknown as IBaseResponse<IUserResponse>
        : { ...response, result: this.mapUser(response.result) }),
    );
  }

  deleteUser(id: number): Observable<IBaseResponse<unknown>> {
    return this.http.delete<IBaseResponse<unknown>>(`${this.API_BASE_URL}/${id}`, { context: this.skipToastContext() });
  }

  private skipToastContext(): HttpContext {
    return new HttpContext().set(SKIP_BUSINESS_ERROR_TOAST, true);
  }

  private mapUser(user: IUserBackendResponse): IUserResponse {
    return {
      id: user.id,
      name: user.name,
      email: user.email,
      role: user.role,
      status: user.status,
      updateAt: user.updateAt ?? user.updatedAt ?? '',
    };
  }
}
