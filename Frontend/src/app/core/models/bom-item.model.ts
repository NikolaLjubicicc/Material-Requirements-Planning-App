export interface BomItem {
  id: number;
  parentItemId: number;
  parentItemSku: string;
  parentItemName: string;
  componentItemId: number;
  componentItemSku: string;
  componentItemName: string;
  quantity: number;
}

export interface BomItemCreateRequest {
  parentItemId: number;
  componentItemId: number;
  quantity: number;
}
