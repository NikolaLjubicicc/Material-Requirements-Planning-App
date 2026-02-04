import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { BomItem } from '../../../core/models';
import { BomService, NotificationService } from '../../../core/services';

@Component({
  selector: 'app-bom-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule
  ],
  templateUrl: './bom-list.component.html',
  styleUrls: ['./bom-list.component.scss']
})
export class BomListComponent implements OnInit {
  bomItems: BomItem[] = [];
  displayedColumns = ['parent', 'component', 'quantity', 'actions'];

  constructor(
    private bomService: BomService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadBomItems();
  }

  loadBomItems(): void {
    this.bomService.getAll().subscribe(items => this.bomItems = items);
  }

  deleteBom(bom: BomItem): void {
    if (confirm(`Da li ste sigurni da želite obrisati ovu sastavnicu?`)) {
      this.bomService.delete(bom.id).subscribe(() => {
        this.notificationService.success('Sastavnica obrisana');
        this.loadBomItems();
      });
    }
  }
}
