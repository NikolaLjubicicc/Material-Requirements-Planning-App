import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTableModule } from '@angular/material/table';
import { ProductionPlan, PlanStatus, PlannedOrder } from '../../../core/models';
import { ProductionPlanService, MrpService, NotificationService } from '../../../core/services';

@Component({
  selector: 'app-plan-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatTableModule
  ],
  templateUrl: './plan-detail.component.html',
  styleUrls: ['./plan-detail.component.scss']
})
export class PlanDetailComponent implements OnInit {
  plan: ProductionPlan | null = null;
  plannedOrders: PlannedOrder[] = [];
  orderColumns = ['item', 'type', 'quantity', 'dueDate'];

  constructor(
    private planService: ProductionPlanService,
    private mrpService: MrpService,
    private notificationService: NotificationService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPlan(+id);
    }
  }

  loadPlan(id: number): void {
    this.planService.getById(id).subscribe(plan => {
      this.plan = plan;
      this.loadOrders(id);
    });
  }

  loadOrders(planId: number): void {
    this.mrpService.getOrdersByPlan(planId).subscribe(orders => this.plannedOrders = orders);
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

  cancelPlan(): void {
    if (this.plan && confirm('Da li ste sigurni da želite otkazati ovaj plan?')) {
      this.planService.cancel(this.plan.id).subscribe(plan => {
        this.plan = plan;
        this.notificationService.success('Plan otkazan');
      });
    }
  }
}
