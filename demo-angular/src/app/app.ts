import { Component, signal, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ToastService } from './shared/services/toast.service';
import { NavigationComponent } from './shared/components/navigation/navigation.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastModule, NavigationComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
  providers: [MessageService],
})
export class App implements OnInit {
  protected readonly title = signal('ele-angular-demo');

  constructor(
    private messageService: MessageService,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.toastService.toast$.subscribe((message) => {
      this.messageService.add(message);
    });
  }
}
