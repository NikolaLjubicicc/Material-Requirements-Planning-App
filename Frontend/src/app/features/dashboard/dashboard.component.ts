import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatGridListModule } from '@angular/material/grid-list';
import { ItemService, BomService, InventoryService, ProductionPlanService } from '../../core/services';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatGridListModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  itemsCount = 0;
  bomCount = 0;
  inventoryCount = 0;
  plansCount = 0;

  constructor(
    private itemService: ItemService,
    private bomService: BomService,
    private inventoryService: InventoryService,
    private planService: ProductionPlanService
  ) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.itemService.getAll().subscribe(items => this.itemsCount = items.length);
    this.bomService.getAll().subscribe(boms => this.bomCount = boms.length);
    this.inventoryService.getAll().subscribe(inv => this.inventoryCount = inv.length);
    this.planService.getAll().subscribe(plans => this.plansCount = plans.length);
  }
}
