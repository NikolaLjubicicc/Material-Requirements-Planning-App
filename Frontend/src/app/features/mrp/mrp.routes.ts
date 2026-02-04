import { Routes } from '@angular/router';

export const MRP_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./mrp-dashboard/mrp-dashboard.component')
      .then(m => m.MrpDashboardComponent)
  },
  {
    path: 'run/:planId',
    loadComponent: () => import('./mrp-run/mrp-run.component')
      .then(m => m.MrpRunComponent)
  },
  {
    path: 'orders',
    loadComponent: () => import('./planned-orders/planned-orders.component')
      .then(m => m.PlannedOrdersComponent)
  }
];
