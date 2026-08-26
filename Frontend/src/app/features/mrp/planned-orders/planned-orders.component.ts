import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PlannedOrder, OrderStatus, StatusUpdateResponse } from '../../../core/models';
import { MrpService } from '../../../core/services';
import { InventoryOperationsDialogComponent } from './inventory-operations-dialog.component';

@Component({
  selector: 'app-planned-orders',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatTabsModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule
  ],
  templateUrl: './planned-orders.component.html',
  styleUrls: ['./planned-orders.component.scss']
})
export class PlannedOrdersComponent implements OnInit {
  allOrders: PlannedOrder[] = [];
  purchaseOrders: PlannedOrder[] = [];
  productionOrders: PlannedOrder[] = [];
  activeOrders: PlannedOrder[] = [];
  activePurchase: PlannedOrder[] = [];
  activeProduction: PlannedOrder[] = [];
  completedOrders: PlannedOrder[] = [];
  cancelledOrders: PlannedOrder[] = [];
  displayedOrders: PlannedOrder[] = [];
  displayedColumns = ['id', 'itemSku', 'itemName', 'orderType', 'quantity', 'startDate', 'dueDate', 'status', 'actions'];
  selectedTab = 0;

  constructor(
    private mrpService: MrpService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.mrpService.getPurchaseOrders().subscribe(orders => {
      this.purchaseOrders = orders;
      this.updateAllOrders();
    });

    this.mrpService.getProductionOrders().subscribe(orders => {
      this.productionOrders = orders;
      this.updateAllOrders();
    });
  }

  updateAllOrders(): void {
    this.allOrders = [...this.purchaseOrders, ...this.productionOrders];
    const activeStatuses = ['PLANNED', 'RELEASED', 'IN_PROGRESS'];
    this.activeOrders = this.allOrders.filter(o => activeStatuses.includes(o.status));
    this.activePurchase = this.purchaseOrders.filter(o => activeStatuses.includes(o.status));
    this.activeProduction = this.productionOrders.filter(o => activeStatuses.includes(o.status));
    this.completedOrders = this.allOrders.filter(o => o.status === 'COMPLETED');
    this.cancelledOrders = this.allOrders.filter(o => o.status === 'CANCELLED');
    this.applyFilter();
  }

  onTabChange(index: number): void {
    this.selectedTab = index;
    this.applyFilter();
  }

  applyFilter(): void {
    switch (this.selectedTab) {
      case 0:
        this.displayedOrders = this.activeOrders;
        break;
      case 1:
        this.displayedOrders = this.activePurchase;
        break;
      case 2:
        this.displayedOrders = this.activeProduction;
        break;
      case 3:
        this.displayedOrders = this.completedOrders;
        break;
      case 4:
        this.displayedOrders = this.cancelledOrders;
        break;
    }
  }

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

  getNextActions(status: string): { label: string; status: OrderStatus; icon: string; color: string }[] {
    switch (status) {
      case 'PLANNED':
        return [
          { label: 'Pusti', status: OrderStatus.RELEASED, icon: 'send', color: 'primary' },
          { label: 'Otkaži', status: OrderStatus.CANCELLED, icon: 'cancel', color: 'warn' }
        ];
      case 'RELEASED':
        return [
          { label: 'Započni', status: OrderStatus.IN_PROGRESS, icon: 'play_arrow', color: 'accent' },
          { label: 'Otkaži', status: OrderStatus.CANCELLED, icon: 'cancel', color: 'warn' }
        ];
      case 'IN_PROGRESS':
        return [
          { label: 'Završi', status: OrderStatus.COMPLETED, icon: 'check_circle', color: 'primary' },
          { label: 'Otkaži', status: OrderStatus.CANCELLED, icon: 'cancel', color: 'warn' }
        ];
      default:
        return [];
    }
  }

  changeStatus(order: PlannedOrder, newStatus: OrderStatus): void {
    console.log('changeStatus called:', order.id, newStatus);
    this.mrpService.updateOrderStatus(order.id, newStatus).subscribe({
      next: (response: StatusUpdateResponse) => {
        this.snackBar.open(response.message, 'OK', { duration: 4000 });
        if (response.inventoryOperations && response.inventoryOperations.length > 0) {
          this.showInventoryOperations(response);
        }
        this.loadOrders();
      },
      error: (err) => {
        const msg = err.error?.message || err.error || 'Greška pri promeni statusa';
        this.snackBar.open(msg, 'Zatvori', { duration: 5000 });
      }
    });
  }

  showInventoryOperations(response: StatusUpdateResponse): void {
    const ops = response.inventoryOperations.map(op =>
      `${op.description}: ${op.itemName} (${op.itemSku}) - ${op.quantity > 0 ? '+' : ''}${op.quantity}`
    ).join('\n');

    this.dialog.open(InventoryOperationsDialogComponent, {
      width: '500px',
      data: response
    });
  }
}
