import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { Item, ItemCategory } from '../../../core/models';
import { ItemService, ProductionPlanService, NotificationService } from '../../../core/services';
import { format } from 'date-fns';

@Component({
  selector: 'app-plan-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule
  ],
  templateUrl: './plan-form.component.html',
  styleUrls: ['./plan-form.component.scss']
})
export class PlanFormComponent implements OnInit {
  form: FormGroup;
  items: Item[] = [];

  constructor(
    private fb: FormBuilder,
    private itemService: ItemService,
    private planService: ProductionPlanService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.form = this.fb.group({
      itemId: ['', Validators.required],
      requiredQuantity: [1, [Validators.required, Validators.min(1)]],
      dueDate: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.itemService.getAll().subscribe(items => {
      this.items = items.filter(i => i.category === ItemCategory.FINISHED_PRODUCT || i.category === ItemCategory.SEMI_FINISHED);
    });
  }

  getCategoryLabel(category: ItemCategory): string {
    const labels: Record<string, string> = {
      'RAW_MATERIAL': 'Sirovina',
      'SEMI_FINISHED': 'Poluproizvod',
      'FINISHED_PRODUCT': 'Gotov proizvod'
    };
    return labels[category] || category;
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const formValue = this.form.value;
    const data = {
      itemId: formValue.itemId,
      requiredQuantity: formValue.requiredQuantity,
      dueDate: format(formValue.dueDate, 'yyyy-MM-dd')
    };

    this.planService.create(data).subscribe(() => {
      this.notificationService.success('Plan proizvodnje uspešno kreiran');
      this.router.navigate(['/production-plans']);
    });
  }
}
