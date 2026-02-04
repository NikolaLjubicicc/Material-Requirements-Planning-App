import { Routes } from '@angular/router';

export const PRODUCTION_PLANS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./plan-list/plan-list.component')
      .then(m => m.PlanListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./plan-form/plan-form.component')
      .then(m => m.PlanFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./plan-detail/plan-detail.component')
      .then(m => m.PlanDetailComponent)
  }
];
