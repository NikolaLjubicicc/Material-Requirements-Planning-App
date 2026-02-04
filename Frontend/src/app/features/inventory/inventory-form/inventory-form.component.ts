import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Item } from '../../../core/models';
import { ItemService, InventoryService, NotificationService } from '../../../core/services';

@Component({
  selector: 'app-inventory-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './inventory-form.component.html',
  styleUrls: ['./inventory-form.component.scss']
})
export class InventoryFormComponent implements OnInit {
  form: FormGroup;
  items: Item[] = [];

  constructor(
    private fb: FormBuilder,
    private itemService: ItemService,
    private inventoryService: InventoryService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.form = this.fb.group({
      itemId: ['', Validators.required],
      quantityOnHand: [0, [Validators.required, Validators.min(0)]],
      reservedQuantity: [0, Validators.min(0)]
    });
  }

  ngOnInit(): void {
    this.itemService.getAll().subscribe(items => this.items = items);
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.inventoryService.create(this.form.value).subscribe(() => {
      this.notificationService.success('Zaliha uspešno kreirana');
      this.router.navigate(['/inventory']);
    });
  }
}
