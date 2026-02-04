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
import { ItemService, BomService, NotificationService } from '../../../core/services';

@Component({
  selector: 'app-bom-form',
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
  templateUrl: './bom-form.component.html',
  styleUrls: ['./bom-form.component.scss']
})
export class BomFormComponent implements OnInit {
  form: FormGroup;
  items: Item[] = [];

  constructor(
    private fb: FormBuilder,
    private itemService: ItemService,
    private bomService: BomService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.form = this.fb.group({
      parentId: ['', Validators.required],
      componentId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.itemService.getAll().subscribe(items => this.items = items);
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const data = this.form.value;

    if (data.parentId === data.componentId) {
      this.notificationService.error('Roditelj i komponenta ne mogu biti isti artikal');
      return;
    }

    this.bomService.create(data).subscribe(() => {
      this.notificationService.success('Sastavnica uspešno kreirana');
      this.router.navigate(['/bom']);
    });
  }
}
