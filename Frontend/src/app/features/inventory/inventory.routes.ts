import { Routes } from '@angular/router';

export const INVENTORY_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./inventory-list/inventory-list.component')
      .then(m => m.InventoryListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./inventory-form/inventory-form.component')
      .then(m => m.InventoryFormComponent)
  },
  {
    path: 'adjust/:itemId',
    loadComponent: () => import('./inventory-adjustment/inventory-adjustment.component')
      .then(m => m.InventoryAdjustmentComponent)
  }
];
