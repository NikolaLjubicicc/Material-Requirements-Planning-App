import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { Item, ItemCategory } from '../../../core/models';
import { ItemService } from '../../../core/services';

@Component({
  selector: 'app-item-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './item-list.component.html',
  styleUrls: ['./item-list.component.scss']
})
export class ItemListComponent implements OnInit {
  items: Item[] = [];
  filteredItems: Item[] = [];
  searchTerm = '';
  selectedCategory: string | null = null;
  displayedColumns = ['sku', 'name', 'category', 'unitOfMeasure', 'leadTimeDays', 'safetyStock', 'actions'];

  constructor(private itemService: ItemService) {}

  ngOnInit(): void {
    this.loadItems();
  }

  loadItems(): void {
    this.itemService.getAll().subscribe(items => {
      this.items = items;
      this.applyFilter();
    });
  }

  applyFilter(): void {
    this.filteredItems = this.items.filter(item => {
      const matchesSearch = !this.searchTerm ||
        item.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        item.sku.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesCategory = !this.selectedCategory || item.category === this.selectedCategory;
      return matchesSearch && matchesCategory;
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

  deleteItem(item: Item): void {
    if (confirm(`Da li ste sigurni da želite obrisati artikal "${item.name}"?`)) {
      this.itemService.delete(item.id).subscribe(() => {
        this.loadItems();
      });
    }
  }
}
