import { Routes } from '@angular/router';

export const BOM_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./bom-list/bom-list.component')
      .then(m => m.BomListComponent)
  },
  {
    path: 'new',
    loadComponent: () => import('./bom-form/bom-form.component')
      .then(m => m.BomFormComponent)
  },
  {
    path: 'tree/:itemId',
    loadComponent: () => import('./bom-tree/bom-tree.component')
      .then(m => m.BomTreeComponent)
  }
];
