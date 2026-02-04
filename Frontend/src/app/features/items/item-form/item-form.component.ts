import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ItemService, NotificationService } from '../../../core/services';
import { ItemCategory } from '../../../core/models';

@Component({
  selector: 'app-item-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './item-form.component.html',
  styleUrls: ['./item-form.component.scss']
})
export class ItemFormComponent implements OnInit {
  form: FormGroup;
  isEditMode = false;
  itemId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private itemService: ItemService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      sku: ['', Validators.required],
      name: ['', Validators.required],
      unitOfMeasure: ['', Validators.required],
      category: ['', Validators.required],
      leadTimeDays: [0, Validators.min(0)],
      safetyStock: [0, Validators.min(0)]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.itemId = +id;
      this.loadItem();
    }
  }

  loadItem(): void {
    if (this.itemId) {
      this.itemService.getById(this.itemId).subscribe(item => {
        this.form.patchValue(item);
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const data = this.form.value;

    if (this.isEditMode && this.itemId) {
      this.itemService.update(this.itemId, data).subscribe(() => {
        this.notificationService.success('Artikal uspešno ažuriran');
        this.router.navigate(['/items']);
      });
    } else {
      this.itemService.create(data).subscribe(() => {
        this.notificationService.success('Artikal uspešno kreiran');
        this.router.navigate(['/items']);
      });
    }
  }
}
