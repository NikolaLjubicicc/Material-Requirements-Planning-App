import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { ProductionPlan, PlanStatus } from '../../../core/models';
import { ProductionPlanService, NotificationService } from '../../../core/services';

@Component({
  selector: 'app-plan-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule
  ],
  templateUrl: './plan-list.component.html',
  styleUrls: ['./plan-list.component.scss']
})
export class PlanListComponent implements OnInit {
  plans: ProductionPlan[] = [];
  filteredPlans: ProductionPlan[] = [];
  selectedTab = 0;
  displayedColumns = ['id', 'item', 'quantity', 'dueDate', 'status', 'actions'];

  constructor(
    private planService: ProductionPlanService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadPlans();
  }

  loadPlans(): void {
    this.planService.getAll().subscribe(plans => {
      this.plans = plans;
      this.applyFilter();
    });
  }

  onTabChange(index: number): void {
    this.selectedTab = index;
    this.applyFilter();
  }

  applyFilter(): void {
    const statusMap: Record<number, PlanStatus | null> = {
      0: null,
      1: PlanStatus.PENDING,
      2: PlanStatus.PROCESSING,
      3: PlanStatus.COMPLETED
    };
    const status = statusMap[this.selectedTab];
    this.filteredPlans = status
      ? this.plans.filter(p => p.status === status)
      : this.plans;
  }

  getStatusLabel(status: PlanStatus): string {
    const labels: Record<string, string> = {
      'PENDING': 'Na čekanju',
      'PROCESSING': 'U obradi',
      'COMPLETED': 'Završen',
      'CANCELLED': 'Otkazan'
    };
    return labels[status] || status;
  }

  cancelPlan(plan: ProductionPlan): void {
    if (confirm(`Da li ste sigurni da želite otkazati plan #${plan.id}?`)) {
      this.planService.cancel(plan.id).subscribe(() => {
        this.notificationService.success('Plan otkazan');
        this.loadPlans();
      });
    }
  }
}
