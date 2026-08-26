import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { StatusUpdateResponse } from '../../../core/models';

@Component({
  selector: 'app-inventory-operations-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule
  ],
  template: `
    <h2 mat-dialog-title>Izvršene operacije na zalihama</h2>
    <mat-dialog-content>
      <p class="status-change">
        Status naloga #{{data.orderId}}:
        <span class="old-status">{{getStatusLabel(data.previousStatus)}}</span>
        <mat-icon class="arrow-icon">arrow_forward</mat-icon>
        <span class="new-status">{{getStatusLabel(data.newStatus)}}</span>
      </p>

      <table mat-table [dataSource]="data.inventoryOperations" class="full-width" *ngIf="data.inventoryOperations.length > 0">
        <ng-container matColumnDef="operation">
          <th mat-header-cell *matHeaderCellDef>Operacija</th>
          <td mat-cell *matCellDef="let op">{{op.operationType}}</td>
        </ng-container>

        <ng-container matColumnDef="item">
          <th mat-header-cell *matHeaderCellDef>Artikal</th>
          <td mat-cell *matCellDef="let op">{{op.itemName}} ({{op.itemSku}})</td>
        </ng-container>

        <ng-container matColumnDef="quantity">
          <th mat-header-cell *matHeaderCellDef>Količina</th>
          <td mat-cell *matCellDef="let op" [class.positive]="op.quantity > 0" [class.negative]="op.quantity < 0">
            {{op.quantity > 0 ? '+' : ''}}{{op.quantity | number:'1.0-3'}}
          </td>
        </ng-container>

        <ng-container matColumnDef="description">
          <th mat-header-cell *matHeaderCellDef>Opis</th>
          <td mat-cell *matCellDef="let op">{{op.description}}</td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="['operation', 'item', 'quantity', 'description']"></tr>
        <tr mat-row *matRowDef="let row; columns: ['operation', 'item', 'quantity', 'description']"></tr>
      </table>

      <p *ngIf="data.inventoryOperations.length === 0" class="no-ops">Nema promena na zalihama.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Zatvori</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .status-change {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      font-size: 1.1em;
    }
    .old-status {
      padding: 4px 12px;
      border-radius: 16px;
      background: #FFCDD2;
      color: #C62828;
    }
    .new-status {
      padding: 4px 12px;
      border-radius: 16px;
      background: #C8E6C9;
      color: #2E7D32;
    }
    .arrow-icon {
      font-size: 20px;
    }
    .full-width {
      width: 100%;
    }
    .positive {
      color: #2E7D32;
      font-weight: bold;
    }
    .negative {
      color: #C62828;
      font-weight: bold;
    }
    .no-ops {
      text-align: center;
      color: #666;
      padding: 16px;
    }
  `]
})
export class InventoryOperationsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<InventoryOperationsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: StatusUpdateResponse
  ) {}

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'PLANNED': 'Planiran',
      'RELEASED': 'Izdat',
      'IN_PROGRESS': 'U toku',
      'COMPLETED': 'Završen',
      'CANCELLED': 'Otkazan'
    };
    return labels[status] || status;
  }
}
