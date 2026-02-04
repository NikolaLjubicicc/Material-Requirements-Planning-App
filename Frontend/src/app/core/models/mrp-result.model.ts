import { PlannedOrder } from './planned-order.model';

export interface MrpResult {
  productionPlanId: number;
  itemName: string;
  itemSku: string;
  totalOrdersGenerated: number;
  purchaseOrdersCount: number;
  productionOrdersCount: number;
  orders: PlannedOrder[];
  status: string;
}
