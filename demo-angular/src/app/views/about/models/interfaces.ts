export interface FormData {
  name: string;
  email: string;
  role: string;
  status: string;
}

export interface FormErrors {
  name?: string;
  email?: string;
  role?: string;
  status?: string;
}

export interface ICascadeSelect {
  label: string;
  value: string;
}
