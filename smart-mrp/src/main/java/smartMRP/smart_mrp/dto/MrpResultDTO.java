package smartMRP.smart_mrp.dto;

import smartMRP.smart_mrp.service.MrpService;

import java.util.List;
import java.util.stream.Collectors;

public class MrpResultDTO {

    private Long productionPlanId;
    private String itemSku;
    private String itemName;
    private String status;
    private int totalOrdersGenerated;
    private int purchaseOrdersCount;
    private int productionOrdersCount;
    private List<PlannedOrderDTO> orders;

    public MrpResultDTO() {}

    public static MrpResultDTO fromMrpResult(MrpService.MrpResult result) {
        MrpResultDTO dto = new MrpResultDTO();
        dto.setProductionPlanId(result.getPlan().getId());
        dto.setItemSku(result.getPlan().getItem().getSku());
        dto.setItemName(result.getPlan().getItem().getName());
        dto.setStatus(result.getPlan().getStatus().name());
        dto.setTotalOrdersGenerated(result.getOrderCount());

        List<PlannedOrderDTO> orderDTOs = result.getGeneratedOrders().stream()
                .map(PlannedOrderDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setOrders(orderDTOs);

        dto.setPurchaseOrdersCount((int) orderDTOs.stream()
                .filter(o -> o.getOrderType().name().equals("PURCHASE"))
                .count());
        dto.setProductionOrdersCount((int) orderDTOs.stream()
                .filter(o -> o.getOrderType().name().equals("PRODUCTION"))
                .count());

        return dto;
    }

    // Getteri i Setteri
    public Long getProductionPlanId() { return productionPlanId; }
    public void setProductionPlanId(Long productionPlanId) { this.productionPlanId = productionPlanId; }

    public String getItemSku() { return itemSku; }
    public void setItemSku(String itemSku) { this.itemSku = itemSku; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalOrdersGenerated() { return totalOrdersGenerated; }
    public void setTotalOrdersGenerated(int totalOrdersGenerated) { this.totalOrdersGenerated = totalOrdersGenerated; }

    public int getPurchaseOrdersCount() { return purchaseOrdersCount; }
    public void setPurchaseOrdersCount(int purchaseOrdersCount) { this.purchaseOrdersCount = purchaseOrdersCount; }

    public int getProductionOrdersCount() { return productionOrdersCount; }
    public void setProductionOrdersCount(int productionOrdersCount) { this.productionOrdersCount = productionOrdersCount; }

    public List<PlannedOrderDTO> getOrders() { return orders; }
    public void setOrders(List<PlannedOrderDTO> orders) { this.orders = orders; }
}
