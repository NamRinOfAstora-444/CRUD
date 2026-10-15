import { Routes } from '@angular/router';
import { HomePageComponent } from './views/home/home-page.component';

export const routes: Routes = [
  {
    path: '',
    component: HomePageComponent,
    title: 'Trang Chủ',
    data: { breadcrumb: 'Trang chủ' },
  },
];
