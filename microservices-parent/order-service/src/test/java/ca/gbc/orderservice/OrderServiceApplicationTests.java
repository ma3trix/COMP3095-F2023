package ca.gbc.orderservice;

import ca.gbc.orderservice.dto.InventoryResponse;
import ca.gbc.orderservice.dto.OrderLineItemDto;
import ca.gbc.orderservice.dto.OrderRequest;
import ca.gbc.orderservice.model.Order;
import ca.gbc.orderservice.repository.OrderRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceApplicationTests extends AbstractContainerBaseTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrderRepository orderRepository;

	private final String TEST_SKU_CODE = "testSkuCode";

	private static MockWebServer mockWebServer;

	private OrderLineItemDto getOrderLineItemDto(String sku) {
		return OrderLineItemDto.builder()
				.id(new Random().nextLong())
				.skuCode(sku)
				.quantity(1)
				.price(BigDecimal.valueOf(100.00))
				.build();
	}

	private OrderRequest getOrderRequest(String sku){
		List<OrderLineItemDto> orderLineItemDtoList = new ArrayList<>();
		orderLineItemDtoList.add(getOrderLineItemDto(sku));
		OrderRequest request = new OrderRequest();
		request.setOrderLineItemDtoList(orderLineItemDtoList);
		return request;
	}

	@BeforeAll
	static void setupServer() throws Exception {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		System.setProperty("inventory.service.url", "http://localhost:" + mockWebServer.getPort());
	}

	@AfterAll
	static void tearDownServer() throws Exception {
		mockWebServer.shutdown();
	}


	@Test
	void placeOrder() throws Exception{

		InventoryResponse inventoryResponse = new InventoryResponse(TEST_SKU_CODE, true);

		mockWebServer.enqueue(new MockResponse().setBody(objectMapper.writeValueAsString(List.of(inventoryResponse)))
				.addHeader("Content-Type", "application/json"));

		OrderRequest orderRequest = getOrderRequest(TEST_SKU_CODE);
		String orderRequestString = objectMapper.writeValueAsString(orderRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
				.content(orderRequestString)
				.contentType("application/json"))
				.andExpect(MockMvcResultMatchers.status().isCreated());

		List<Order> orders = orderRepository.findAll();
		Assertions.assertTrue(orders.size() > 0);
		String returnedSkuCode = orders.get(0).getOrderLineItemList().get(0).getSkuCode();
		Assertions.assertEquals(TEST_SKU_CODE, returnedSkuCode);

	}

}
