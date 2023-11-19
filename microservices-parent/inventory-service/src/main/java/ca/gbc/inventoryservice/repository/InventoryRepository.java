package ca.gbc.inventoryservice.repository;

import ca.gbc.inventoryservice.dto.InventoryRequest;
import ca.gbc.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySkuCode(String skuCode);

    List<Inventory> findAllBySkuCodeIn(List<String> skuCodes);

    @Query("SELECT i FROM Inventory i WHERE i.skuCode = :skuCode AND i.quantity >= :quantity")
    List<Inventory> findBySkuCodeAndQuantity(String skuCode, Integer quantity);

    default List<Inventory> findAllByInventoryRequests(List<InventoryRequest> inventoryRequests){

        return inventoryRequests.stream()
                .flatMap(request -> findBySkuCodeAndQuantity(request.getSkuCode(), request.getQuantity()).stream())
                .collect(Collectors.toList());
    }

}
