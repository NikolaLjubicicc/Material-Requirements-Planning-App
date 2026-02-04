import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MrpResult, PlannedOrder } from '../../../core/models';
import { MrpService, NotificationService } from '../../../core/services';

@Component({
  selector: 'app-mrp-run',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './mrp-run.component.html',
  styleUrls: ['./mrp-run.component.scss']
})
export class MrpRunComponent implements OnInit {
  result: MrpResult | null = null;
  loading = false;
  error: string | null = null;
  orderColumns = ['itemSku', 'itemName', 'orderType', 'quantity', 'startDate', 'dueDate', 'status'];

  constructor(
    private mrpService: MrpService,
    private notificationService: NotificationService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const planId = this.route.snapshot.paramMap.get('planId');
    if (planId) {
      this.runMrp(+planId);
    }
  }

  runMrp(planId: number): void {
    this.loading = true;
    this.error = null;

    this.mrpService.runMrp(planId).subscribe({
      next: (result) => {
        this.result = result;
        this.loading = false;
        this.notificationService.success(`MRP završen! Generisano ${result.totalOrdersGenerated} naloga.`);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Greška prilikom MRP kalkulacije';
      }
    });
  }
}
