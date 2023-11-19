package ca.gbc.inventoryservice;

import ca.gbc.inventoryservice.dto.InventoryRequest;
import ca.gbc.inventoryservice.model.Inventory;
import ca.gbc.inventoryservice.repository.InventoryRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryServiceIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    public void isInStock() throws Exception {
        // Setup inventory data
        Inventory data1 = Inventory.builder()
                .id(14L)
                .quantity(12)
                .skuCode("sku345")
                .build();
        Inventory data2 = Inventory.builder()
                .id(13L)
                .quantity(12)
                .skuCode("sku123")
                .build();
        inventoryRepository.save(data1);
        inventoryRepository.save(data2);

        List<InventoryRequest> inventoryRequestsList = new ArrayList<>();
        inventoryRequestsList.add(new InventoryRequest("sku123", 2));
        inventoryRequestsList.add(new InventoryRequest("sku345", 2));
        String dataSender = objectMapper.writeValueAsString(inventoryRequestsList);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dataSender));

        MvcResult result = response.andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNodes = objectMapper.readTree(jsonResponse);
        Assertions.assertThat(jsonNodes.size()).isEqualTo(2);

        for (JsonNode node : jsonNodes) {
            String skuCode = node.get("skuCode").asText();
            boolean sufficientStock = node.get("sufficientStock").asBoolean();
            Assertions.assertThat(sufficientStock).isTrue();
        }

        inventoryRepository.deleteAll();
    }
}
