export enum PlanStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface ProductionPlan {
  id: number;
  itemId: number;
  itemSku: string;
  itemName: string;
  requiredQuantity: number;
  dueDate: string;
  status: PlanStatus;
  createdAt: string;
}

export interface ProductionPlanCreateRequest {
  itemId: number;
  requiredQuantity: number;
  dueDate: string;
}

export interface ProductionPlanUpdateRequest {
  requiredQuantity?: number;
  dueDate?: string;
}
