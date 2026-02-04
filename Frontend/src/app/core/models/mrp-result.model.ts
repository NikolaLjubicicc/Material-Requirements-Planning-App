import { PlannedOrder } from './planned-order.model';

export interface MrpResult {
  productionPlanId: number;
  productionPlanItem: string;
  requiredQuantity: number;
  dueDate: string;
  totalOrdersGenerated: number;
  purchaseOrdersCount: number;
  productionOrdersCount: number;
  plannedOrders: PlannedOrder[];
  calculatedAt: string;
}
