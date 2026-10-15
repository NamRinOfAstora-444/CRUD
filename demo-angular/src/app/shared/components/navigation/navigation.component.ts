import { Component, signal } from '@angular/core';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { filter } from 'rxjs';
import { MenubarModule } from 'primeng/menubar';
import { MenuItem } from 'primeng/api';
import { BreadcrumbModule } from 'primeng/breadcrumb';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [MenubarModule, BreadcrumbModule],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.css',
})
export class NavigationComponent {
  items = signal<MenuItem[]>([
    {
      label: 'Trang chủ',
      icon: 'pi pi-home',
      routerLink: '/',
      command: () => this.navigateTo('/'),
    },
    {
      label: 'Tạo tài khoản',
      icon: 'pi pi-user-plus',
      routerLink: '/create-user',
      command: () => this.navigateTo('/create-user'),
    },
  ]);

  breadcrumbs = signal<MenuItem[]>([]);
  home = signal<MenuItem>({ icon: 'pi pi-home', routerLink: '/' });

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) {
    // Theo dõi thay đổi route và cập nhật breadcrumb
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      this.updateBreadcrumbs();
    });

    // Set initial breadcrumbs
    this.updateBreadcrumbs();
  }

  private updateBreadcrumbs(): void {
    const breadcrumbs: MenuItem[] = [];
    let currentRoute = this.activatedRoute.root;

    while (currentRoute.firstChild) {
      currentRoute = currentRoute.firstChild;
      const routeData = currentRoute.snapshot.data;
      const routePath = currentRoute.snapshot.url.map((segment) => segment.path).join('/');

      if (routeData['breadcrumb'] && routePath) {
        breadcrumbs.push({
          label: routeData['breadcrumb'],
          routerLink: '/' + routePath,
        });
      }
    }

    this.breadcrumbs.set(breadcrumbs);
  }

  navigateTo(path: string): void {
    void this.router.navigate([path]);
  }
}
