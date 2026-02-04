import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import { NestedTreeControl } from '@angular/cdk/tree';
import { Item, BomItem } from '../../../core/models';
import { ItemService, BomService } from '../../../core/services';

interface BomTreeNode {
  id: number;
  sku: string;
  name: string;
  quantity: number;
  children?: BomTreeNode[];
}

@Component({
  selector: 'app-bom-tree',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTreeModule
  ],
  templateUrl: './bom-tree.component.html',
  styleUrls: ['./bom-tree.component.scss']
})
export class BomTreeComponent implements OnInit {
  rootItem: Item | null = null;
  treeControl = new NestedTreeControl<BomTreeNode>(node => node.children);
  dataSource = new MatTreeNestedDataSource<BomTreeNode>();

  constructor(
    private itemService: ItemService,
    private bomService: BomService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const itemId = this.route.snapshot.paramMap.get('itemId');
    if (itemId) {
      this.loadTree(+itemId);
    }
  }

  hasChild = (_: number, node: BomTreeNode) => !!node.children && node.children.length > 0;

  loadTree(itemId: number): void {
    this.itemService.getById(itemId).subscribe(item => {
      this.rootItem = item;
      this.buildTree(itemId);
    });
  }

  buildTree(parentId: number): void {
    this.bomService.getByParentId(parentId).subscribe(bomItems => {
      const nodes = bomItems.map(bom => this.createNode(bom));
      this.dataSource.data = nodes;
      this.loadChildrenRecursively(nodes);
    });
  }

  createNode(bom: BomItem): BomTreeNode {
    return {
      id: bom.componentItemId,
      sku: bom.componentItemSku,
      name: bom.componentItemName,
      quantity: bom.quantity,
      children: []
    };
  }

  loadChildrenRecursively(nodes: BomTreeNode[]): void {
    nodes.forEach(node => {
      this.bomService.getByParentId(node.id).subscribe(bomItems => {
        if (bomItems.length > 0) {
          node.children = bomItems.map(bom => this.createNode(bom));
          this.dataSource.data = [...this.dataSource.data];
          this.loadChildrenRecursively(node.children);
        }
      });
    });
  }
}
