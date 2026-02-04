import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent {
  getPageTitle(): string {
    const path = window.location.pathname;
    const titles: Record<string, string> = {
      '/dashboard': 'Dashboard',
      '/items': 'Artikli',
      '/bom': 'Sastavnice (BOM)',
      '/inventory': 'Zalihe',
      '/production-plans': 'Planovi proizvodnje',
      '/mrp': 'MRP - Planiranje materijalnih potreba'
    };

    for (const [key, value] of Object.entries(titles)) {
      if (path.startsWith(key)) return value;
    }
    return 'Smart MRP';
  }
}
