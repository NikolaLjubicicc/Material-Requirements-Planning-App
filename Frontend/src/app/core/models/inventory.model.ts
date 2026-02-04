export interface Inventory {
  id: number;
  itemId: number;
  itemSku: string;
  itemName: string;
  quantityOnHand: number;
  reservedQuantity: number;
  availableQuantity: number;
}

export interface InventoryCreateRequest {
  itemId: number;
  quantityOnHand: number;
  reservedQuantity?: number;
}
