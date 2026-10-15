import { Component, input, output, TemplateRef, contentChildren, Input } from '@angular/core';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { DataTableColumn, PageEvent } from './data-table.interface';
import { DataTableColumnDirective } from './data-table-column.directive';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [TableModule, NgTemplateOutlet],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.css',
})
export class DataTableComponent {
  data = input<any[]>([]);
  columns = input<DataTableColumn[]>([]);
  loading = input<boolean>(false);
  rowsPerPage = input<number>(10);
  totalRecords = input<number>(0);
  first = input<number>(0);

  pageChange = output<PageEvent>();

  columnTemplates = contentChildren(DataTableColumnDirective);

  getColumnTemplate(field: string): TemplateRef<any> | null {
    const directive = this.columnTemplates()?.find((t) => t.field === field);
    return directive ? directive.template : null;
  }

  onLazyLoad(event: TableLazyLoadEvent): void {
    const rows = event.rows ?? this.rowsPerPage();
    const first = event.first ?? 0;
    this.pageChange.emit({ page: Math.floor(first / rows), rows });
  }
}
