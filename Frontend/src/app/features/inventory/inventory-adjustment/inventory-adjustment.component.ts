import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { Inventory } from '../../../core/models';
import { InventoryService, NotificationService } from '../../../core/services';

@Component({
  selector: 'app-inventory-adjustment',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatTabsModule,
    MatIconModule
  ],
  templateUrl: './inventory-adjustment.component.html',
  styleUrls: ['./inventory-adjustment.component.scss']
})
export class InventoryAdjustmentComponent implements OnInit {
  inventory: Inventory | null = null;
  itemId: number | null = null;

  addQuantity: number | null = null;
  removeQuantity: number | null = null;
  reserveQuantity: number | null = null;
  releaseQuantity: number | null = null;
  setQuantityValue: number | null = null;

  constructor(
    private inventoryService: InventoryService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const itemId = this.route.snapshot.paramMap.get('itemId');
    if (itemId) {
      this.itemId = +itemId;
      this.loadInventory();
    }
  }

  loadInventory(): void {
    if (this.itemId) {
      this.inventoryService.getByItemId(this.itemId).subscribe(inv => this.inventory = inv);
    }
  }

  addStock(): void {
    if (this.itemId && this.addQuantity) {
      this.inventoryService.addStock(this.itemId, this.addQuantity).subscribe(inv => {
        this.inventory = inv;
        this.notificationService.success('Roba primljena');
        this.addQuantity = null;
      });
    }
  }

  removeStock(): void {
    if (this.itemId && this.removeQuantity) {
      this.inventoryService.removeStock(this.itemId, this.removeQuantity).subscribe(inv => {
        this.inventory = inv;
        this.notificationService.success('Roba izdata');
        this.removeQuantity = null;
      });
    }
  }

  reserve(): void {
    if (this.itemId && this.reserveQuantity) {
      this.inventoryService.reserve(this.itemId, this.reserveQuantity).subscribe(inv => {
        this.inventory = inv;
        this.notificationService.success('Količina rezervisana');
        this.reserveQuantity = null;
      });
    }
  }

  release(): void {
    if (this.itemId && this.releaseQuantity) {
      this.inventoryService.releaseReservation(this.itemId, this.releaseQuantity).subscribe(inv => {
        this.inventory = inv;
        this.notificationService.success('Rezervacija oslobođena');
        this.releaseQuantity = null;
      });
    }
  }

  setQuantity(): void {
    if (this.itemId && this.setQuantityValue !== null) {
      this.inventoryService.setQuantity(this.itemId, this.setQuantityValue).subscribe(inv => {
        this.inventory = inv;
        this.notificationService.success('Količina ažurirana');
        this.setQuantityValue = null;
      });
    }
  }
}
