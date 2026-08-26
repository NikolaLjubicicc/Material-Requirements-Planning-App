export enum OrderType {
  PURCHASE = 'PURCHASE',
  PRODUCTION = 'PRODUCTION'
}

export enum OrderStatus {
  PLANNED = 'PLANNED',
  RELEASED = 'RELEASED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface PlannedOrder {
  id: number;
  itemId: number;
  itemSku: string;
  itemName: string;
  productionPlanId: number | null;
  quantity: number;
  orderType: OrderType;
  startDate: string;
  dueDate: string;
  status: OrderStatus;
  createdAt: string;
}

export interface InventoryOperation {
  operationType: string;
  itemId: number;
  itemSku: string;
  itemName: string;
  quantity: number;
  description: string;
}

export interface StatusUpdateResponse {
  orderId: number;
  previousStatus: OrderStatus;
  newStatus: OrderStatus;
  orderType: OrderType;
  inventoryOperations: InventoryOperation[];
  success: boolean;
  message: string;
}
