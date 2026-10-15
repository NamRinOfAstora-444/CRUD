export interface IBaseResponse<T> {
  code: string;
  message: string;
  result: T;
}

export interface IBasePagination<T> {
  content: T[];
  totalElements: number;
  page: number;
  size: number;
}
