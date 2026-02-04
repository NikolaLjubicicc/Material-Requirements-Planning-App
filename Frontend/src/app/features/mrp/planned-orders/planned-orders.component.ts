import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { PlannedOrder, OrderType } from '../../../core/models';
import { MrpService } from '../../../core/services';

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
    MatTabsModule
  ],
  templateUrl: './planned-orders.component.html',
  styleUrls: ['./planned-orders.component.scss']
})
export class PlannedOrdersComponent implements OnInit {
  allOrders: PlannedOrder[] = [];
  purchaseOrders: PlannedOrder[] = [];
  productionOrders: PlannedOrder[] = [];
  displayedOrders: PlannedOrder[] = [];
  displayedColumns = ['id', 'itemSku', 'itemName', 'orderType', 'quantity', 'startDate', 'dueDate', 'status'];
  selectedTab = 0;

  constructor(private mrpService: MrpService) {}

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
    this.applyFilter();
  }

  onTabChange(index: number): void {
    this.selectedTab = index;
    this.applyFilter();
  }

  applyFilter(): void {
    switch (this.selectedTab) {
      case 0:
        this.displayedOrders = this.allOrders;
        break;
      case 1:
        this.displayedOrders = this.purchaseOrders;
        break;
      case 2:
        this.displayedOrders = this.productionOrders;
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
}
