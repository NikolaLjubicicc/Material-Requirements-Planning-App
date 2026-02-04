import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component')
      .then(m => m.DashboardComponent)
  },
  {
    path: 'items',
    loadChildren: () => import('./features/items/items.routes')
      .then(m => m.ITEMS_ROUTES)
  },
  {
    path: 'bom',
    loadChildren: () => import('./features/bom/bom.routes')
      .then(m => m.BOM_ROUTES)
  },
  {
    path: 'inventory',
    loadChildren: () => import('./features/inventory/inventory.routes')
      .then(m => m.INVENTORY_ROUTES)
  },
  {
    path: 'production-plans',
    loadChildren: () => import('./features/production-plans/production-plans.routes')
      .then(m => m.PRODUCTION_PLANS_ROUTES)
  },
  {
    path: 'mrp',
    loadChildren: () => import('./features/mrp/mrp.routes')
      .then(m => m.MRP_ROUTES)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
