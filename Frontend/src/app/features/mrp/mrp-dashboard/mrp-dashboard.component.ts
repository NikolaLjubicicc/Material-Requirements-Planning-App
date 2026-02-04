import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { ProductionPlan } from '../../../core/models';
import { ProductionPlanService } from '../../../core/services';

@Component({
  selector: 'app-mrp-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule
  ],
  templateUrl: './mrp-dashboard.component.html',
  styleUrls: ['./mrp-dashboard.component.scss']
})
export class MrpDashboardComponent implements OnInit {
  pendingPlans: ProductionPlan[] = [];
  displayedColumns = ['id', 'item', 'quantity', 'dueDate', 'actions'];

  constructor(private planService: ProductionPlanService) {}

  ngOnInit(): void {
    this.loadPendingPlans();
  }

  loadPendingPlans(): void {
    this.planService.getPending().subscribe(plans => this.pendingPlans = plans);
  }
}
