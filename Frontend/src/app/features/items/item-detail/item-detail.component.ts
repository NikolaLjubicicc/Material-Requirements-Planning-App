import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { Item, ItemCategory, BomItem } from '../../../core/models';
import { ItemService, BomService } from '../../../core/services';

@Component({
  selector: 'app-item-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule
  ],
  templateUrl: './item-detail.component.html',
  styleUrls: ['./item-detail.component.scss']
})
export class ItemDetailComponent implements OnInit {
  item: Item | null = null;
  components: BomItem[] = [];
  usedIn: BomItem[] = [];

  constructor(
    private itemService: ItemService,
    private bomService: BomService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadItem(+id);
    }
  }

  loadItem(id: number): void {
    this.itemService.getById(id).subscribe(item => {
      this.item = item;
      this.loadBomData(id);
    });
  }

  loadBomData(itemId: number): void {
    this.bomService.getByParentId(itemId).subscribe(bom => this.components = bom);
    this.bomService.getByComponentId(itemId).subscribe(bom => this.usedIn = bom);
  }

  getCategoryLabel(category: ItemCategory): string {
    const labels: Record<string, string> = {
      'RAW_MATERIAL': 'Sirovina',
      'SEMI_FINISHED': 'Poluproizvod',
      'FINISHED_PRODUCT': 'Gotov proizvod'
    };
    return labels[category] || category;
  }
}
