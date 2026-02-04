export enum ItemCategory {
  RAW_MATERIAL = 'RAW_MATERIAL',
  SEMI_FINISHED = 'SEMI_FINISHED',
  FINISHED_PRODUCT = 'FINISHED_PRODUCT'
}

export interface Item {
  id: number;
  sku: string;
  name: string;
  unitOfMeasure: string;
  category: ItemCategory;
  leadTimeDays: number;
  safetyStock: number;
}

export interface ItemCreateRequest {
  sku: string;
  name: string;
  unitOfMeasure: string;
  category: ItemCategory;
  leadTimeDays?: number;
  safetyStock?: number;
}

export interface ItemUpdateRequest {
  sku?: string;
  name?: string;
  unitOfMeasure?: string;
  category?: ItemCategory;
  leadTimeDays?: number;
  safetyStock?: number;
}
