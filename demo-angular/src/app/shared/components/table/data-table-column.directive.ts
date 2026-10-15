import { Directive, Input, TemplateRef } from '@angular/core';

@Directive({
  selector: '[appDataTableColumn]',
  standalone: true,
})
export class DataTableColumnDirective {
  @Input() appDataTableColumn!: string;

  constructor(public template: TemplateRef<any>) {}

  get field(): string {
    return this.appDataTableColumn;
  }
}

