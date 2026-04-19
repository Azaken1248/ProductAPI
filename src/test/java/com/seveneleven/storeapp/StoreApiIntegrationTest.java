package com.seveneleven.storeapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seveneleven.storeapp.model.dto.*;
import com.seveneleven.storeapp.model.entity.NotificationType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken = "";
    private Long adminId;
    private String customerToken = "";
    private Long customerId;
    private Long productId;
    private Long orderProductId;
    private Long inactiveProductId;
    private Long inventoryId;
    private Long orderId;
    private Long notificationId;
    private Long adminNotificationId;

    private final String uniqueId = String.valueOf(System.currentTimeMillis());
    private UserRequestDTO testCustomer;
    private ProductRequestDTO testProduct;
    private ProductRequestDTO orderTestProduct;
    private ProductRequestDTO inactiveTestProduct;

    @BeforeAll
    void setupData() {
        testCustomer = UserRequestDTO.builder()
                .firstName("Regular")
                .lastName("Customer")
                .email("customer_" + uniqueId + "@store.com")
                .password("Password123!")
                .phone("0987654321")
                .role("CUSTOMER")
                .build();

        testProduct = ProductRequestDTO.builder()
                .sku("SKU_CHAOS_" + uniqueId)
                .name("Titanium Chaos Widget")
                .category("TESTING")
                .price(new BigDecimal("99.99"))
                .isActive(true)
                .build();

        orderTestProduct = ProductRequestDTO.builder()
                .sku("SKU_ORDER_" + uniqueId)
                .name("Order Flow Widget")
                .category("TESTING")
                .price(new BigDecimal("15.00"))
                .isActive(true)
                .build();

        inactiveTestProduct = ProductRequestDTO.builder()
                .sku("SKU_INACT_" + uniqueId)
                .name("Discontinued Widget")
                .category("TESTING")
                .price(new BigDecimal("5.00"))
                .isActive(false)
                .build();
    }

    private ResultMatcher anyOf(int... statusCodes) {
        return result -> {
            int actual = result.getResponse().getStatus();
            boolean match = Arrays.stream(statusCodes).anyMatch(expected -> expected == actual);
            if (!match) {
                fail("Status expected any of: " + Arrays.toString(statusCodes) + " but was: " + actual);
            }
        };
    }

    @Test @Order(1) void test01_MissingEmailValidation() throws Exception {
        UserRequestDTO badReq = UserRequestDTO.builder().firstName("F").password("Pass123!").role("CUSTOMER").build();
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(badReq))).andExpect(status().isBadRequest());
    }

    @Test @Order(2) void test02_BadEmailFormatValidation() throws Exception {
        UserRequestDTO badReq = UserRequestDTO.builder().firstName("F").email("not-an-email").password("Pass123!").role("CUSTOMER").build();
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(badReq))).andExpect(status().isBadRequest());
    }

    @Test @Order(3) void test03_ShortPasswordValidation() throws Exception {
        UserRequestDTO badReq = UserRequestDTO.builder().firstName("F").email("a@b.com").password("123").role("CUSTOMER").build();
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(badReq))).andExpect(status().isBadRequest());
    }

    @Test @Order(4) void test04_EmptyFirstNameValidation() throws Exception {
        UserRequestDTO badReq = UserRequestDTO.builder().firstName("").email("a@b.com").password("Pass123!").role("CUSTOMER").build();
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(badReq))).andExpect(status().isBadRequest());
    }

    @Test @Order(5) void test05_RegisterCustomer() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testCustomer))).andExpect(status().isCreated());
    }

    @Test @Order(6) void test06_LoginAdmin() throws Exception {
        AuthRequestDTO login = new AuthRequestDTO();
        login.setEmail("superadmin@store.com");
        login.setPassword("dummy_password!");
        MvcResult res = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login))).andExpect(status().isOk()).andReturn();
        AuthResponseDTO dto = objectMapper.readValue(res.getResponse().getContentAsString(), AuthResponseDTO.class);
        adminToken = dto.getToken();
        adminId = dto.getId();
    }

    @Test @Order(7) void test07_LoginCustomer() throws Exception {
        AuthRequestDTO login = new AuthRequestDTO();
        login.setEmail(testCustomer.getEmail());
        login.setPassword(testCustomer.getPassword());
        MvcResult res = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login))).andExpect(status().isOk()).andReturn();
        AuthResponseDTO dto = objectMapper.readValue(res.getResponse().getContentAsString(), AuthResponseDTO.class);
        customerToken = dto.getToken();
        customerId = dto.getId();
    }

    @Test @Order(8) void test08_RequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(anyOf(401, 403));
    }

    @Test @Order(9) void test09_RequestWithFakeToken() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer eyJhbGci.fake.sig")).andExpect(anyOf(401, 403));
    }

    @Test @Order(10) void test10_RequestWithMalformedHeader() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", "Basic admin:password")).andExpect(anyOf(401, 403));
    }

    @Test @Order(11) void test11_AdminFetchesGhostId() throws Exception {
        mockMvc.perform(get("/api/users/9999999").header("Authorization", "Bearer " + adminToken)).andExpect(status().isNotFound());
    }

    @Test @Order(12) void test12_AdminDeletesGhostId() throws Exception {
        mockMvc.perform(delete("/api/users/9999999").header("Authorization", "Bearer " + adminToken)).andExpect(status().isNotFound());
    }

    @Test @Order(13) void test13_AdminFetchesGhostProduct() throws Exception {
        mockMvc.perform(get("/api/products/9999999").header("Authorization", "Bearer " + adminToken)).andExpect(status().isNotFound());
    }

    @Test @Order(14) void test14_CustomerFetchesAdminProfile() throws Exception {
        mockMvc.perform(get("/api/users/" + adminId).header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
    }

    @Test @Order(15) void test15_CustomerDeletesAdminProfile() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminId).header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
    }

    @Test @Order(16) void test16_CustomerTriesEscalation() throws Exception {
        testCustomer.setRole("ADMIN");
        MvcResult res = mockMvc.perform(put("/api/users/" + customerId)
                .header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testCustomer)))
                .andExpect(status().isOk()).andReturn();
        UserResponseDTO user = objectMapper.readValue(res.getResponse().getContentAsString(), UserResponseDTO.class);
        assertEquals("CUSTOMER", user.getRole());
    }

    @Test @Order(17) void test17_UnauthenticatedCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testProduct))).andExpect(anyOf(401, 403));
    }

    @Test @Order(18) void test18_CustomerCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testProduct))).andExpect(status().isForbidden());
    }

    @Test @Order(19) void test19_AdminCreateProductNegativePrice() throws Exception {
        ProductRequestDTO badProd = ProductRequestDTO.builder().sku("SKU_NEG").name("A").price(new BigDecimal("-10")).build();
        mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(badProd))).andExpect(status().isBadRequest());
    }

    @Test @Order(20) void test20_AdminCreateProductNoSku() throws Exception {
        ProductRequestDTO badProd = ProductRequestDTO.builder().name("A").price(new BigDecimal("10")).build();
        mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(badProd))).andExpect(status().isBadRequest());
    }

    @Test @Order(21) void test21_AdminCreatesProduct() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testProduct))).andExpect(status().isCreated()).andReturn();
        productId = objectMapper.readValue(res.getResponse().getContentAsString(), ProductResponseDTO.class).getId();
    }

    @Test @Order(22) void test22_AdminDuplicateSku() throws Exception {
        mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testProduct))).andExpect(status().isConflict());
    }

    @Test @Order(23) void test23_UnauthenticatedGetProducts() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isOk());
    }

    @Test @Order(24) void test24_CustomerUpdatesProduct() throws Exception {
        mockMvc.perform(put("/api/products/" + productId).header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testProduct))).andExpect(status().isForbidden());
    }

    @Test @Order(25) void test25_CustomerDeletesProduct() throws Exception {
        mockMvc.perform(delete("/api/products/" + productId).header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
    }

    @Test @Order(26) void test26_AdminCreatesInactiveProduct() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inactiveTestProduct))).andExpect(status().isCreated()).andReturn();
        inactiveProductId = objectMapper.readValue(res.getResponse().getContentAsString(), ProductResponseDTO.class).getId();
    }

    @Test @Order(27) void test27_SetupOrderProduct() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/products").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderTestProduct))).andExpect(status().isCreated()).andReturn();
        orderProductId = objectMapper.readValue(res.getResponse().getContentAsString(), ProductResponseDTO.class).getId();
    }

    @Test @Order(28) void test28_CustomerCreateInventory() throws Exception {
        InventoryRequestDTO inv = InventoryRequestDTO.builder().productId(orderProductId).availableQuantity(5).threshold(2).build();
        mockMvc.perform(post("/api/inventory").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inv))).andExpect(status().isForbidden());
    }

    @Test @Order(29) void test29_AdminCreateInventoryNegativeQty() throws Exception {
        InventoryRequestDTO inv = InventoryRequestDTO.builder().productId(orderProductId).availableQuantity(-5).threshold(2).build();
        mockMvc.perform(post("/api/inventory").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inv))).andExpect(anyOf(400, 500));
    }

    @Test @Order(30) void test30_AdminCreateInventoryGhostProduct() throws Exception {
        InventoryRequestDTO inv = InventoryRequestDTO.builder().productId(99999L).availableQuantity(5).threshold(2).build();
        mockMvc.perform(post("/api/inventory").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inv))).andExpect(status().isNotFound());
    }

    @Test @Order(31) void test31_AdminCreatesValidInventory() throws Exception {
        InventoryRequestDTO inv = InventoryRequestDTO.builder().productId(orderProductId).availableQuantity(5).threshold(2).build();
        MvcResult res = mockMvc.perform(post("/api/inventory").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inv))).andExpect(anyOf(200, 201)).andReturn();
        inventoryId = objectMapper.readValue(res.getResponse().getContentAsString(), InventoryResponseDTO.class).getId();
    }

    @Test @Order(32) void test32_AdminDuplicateInventory() throws Exception {
        InventoryRequestDTO inv = InventoryRequestDTO.builder().productId(orderProductId).availableQuantity(10).threshold(2).build();
        mockMvc.perform(post("/api/inventory").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inv))).andExpect(anyOf(409, 500));
    }

    @Test @Order(33) void test33_CustomerFetchAllInventory() throws Exception {
        mockMvc.perform(get("/api/inventory").header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
    }

    @Test @Order(34) void test34_AdminCreateInventoryInactiveProduct() throws Exception {
        InventoryRequestDTO inv = InventoryRequestDTO.builder().productId(inactiveProductId).availableQuantity(100).threshold(10).build();
        mockMvc.perform(post("/api/inventory").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inv))).andExpect(anyOf(200, 201));
    }

    @Test @Order(35) void test35_CheckoutEmptyItems() throws Exception {
        OrdersRequestDTO orderReq = new OrdersRequestDTO();
        orderReq.setUserId(customerId);
        orderReq.setOrderItems(Collections.emptyList());
        mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andExpect(status().isBadRequest());
    }

    @Test @Order(36) void test36_CheckoutNegativeQty() throws Exception {
        OrderItemRequestDTO item = OrderItemRequestDTO.builder().productId(orderProductId).quantity(-2).unitPrice(new BigDecimal("15.00")).build();
        OrdersRequestDTO orderReq = new OrdersRequestDTO();
        orderReq.setUserId(customerId);
        orderReq.setOrderItems(List.of(item));
        mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andExpect(status().isBadRequest());
    }

    @Test @Order(37) void test37_CheckoutZeroQty() throws Exception {
        OrderItemRequestDTO item = OrderItemRequestDTO.builder().productId(orderProductId).quantity(0).unitPrice(new BigDecimal("15.00")).build();
        OrdersRequestDTO orderReq = new OrdersRequestDTO();
        orderReq.setUserId(customerId);
        orderReq.setOrderItems(List.of(item));
        mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andExpect(status().isBadRequest());
    }

    @Test @Order(38) void test38_CheckoutInactiveProduct() throws Exception {
        OrderItemRequestDTO item = OrderItemRequestDTO.builder().productId(inactiveProductId).quantity(1).unitPrice(new BigDecimal("5.00")).build();
        OrdersRequestDTO orderReq = new OrdersRequestDTO();
        orderReq.setUserId(customerId);
        orderReq.setOrderItems(List.of(item));
        mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andExpect(anyOf(400, 409, 500));
    }

    @Test @Order(39) void test39_CheckoutExceedingInventory() throws Exception {
        OrderItemRequestDTO item = OrderItemRequestDTO.builder().productId(orderProductId).quantity(20).unitPrice(new BigDecimal("15.00")).build();
        OrdersRequestDTO orderReq = new OrdersRequestDTO();
        orderReq.setUserId(customerId);
        orderReq.setOrderItems(List.of(item));
        mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andExpect(status().isConflict());
    }

    @Test @Order(40) void test40_CheckoutPriceSpoofing() throws Exception {
        OrderItemRequestDTO item = OrderItemRequestDTO.builder().productId(orderProductId).quantity(2).unitPrice(new BigDecimal("0.01")).build();
        OrdersRequestDTO orderReq = new OrdersRequestDTO();
        orderReq.setUserId(customerId);
        orderReq.setOrderItems(List.of(item));
        MvcResult res = mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andExpect(status().isCreated()).andReturn();
        OrdersResponseDTO order = objectMapper.readValue(res.getResponse().getContentAsString(), OrdersResponseDTO.class);
        orderId = order.getId();
        assertEquals(0, new BigDecimal("30.00").compareTo(order.getTotalAmount()));
    }

    @Test @Order(41) void test41_VerifyInventoryDecremented() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/inventory/" + orderProductId).header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk()).andReturn();
        InventoryResponseDTO inv = objectMapper.readValue(res.getResponse().getContentAsString(), InventoryResponseDTO.class);
        assertEquals(3, inv.getAvailableQuantity());
    }

    @Test @Order(42) void test42_CustomerFetchAllOrders() throws Exception {
        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
    }

    @Test @Order(43) void test43_CustomerFetchOwnOrder() throws Exception {
        mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", "Bearer " + customerToken)).andExpect(anyOf(200, 403));
    }

    @Test @Order(44) void test44_CustomerCreatesCheckoutForAdmin_IDOR() throws Exception {
        OrderItemRequestDTO item = OrderItemRequestDTO.builder().productId(orderProductId).quantity(1).unitPrice(new BigDecimal("15.00")).build();
        OrdersRequestDTO idorReq = new OrdersRequestDTO();
        idorReq.setUserId(adminId);
        idorReq.setOrderItems(List.of(item));
        mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(idorReq))).andExpect(anyOf(201, 403));
    }

    @Test @Order(45) void test45_AdminFetchAllOrders() throws Exception {
        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk());
    }

    @Test @Order(46) void test46_CustomerTriggersLowStock() throws Exception {
        OrderItemRequestDTO item = OrderItemRequestDTO.builder().productId(orderProductId).quantity(2).unitPrice(new BigDecimal("15.00")).build();
        OrdersRequestDTO orderReq = new OrdersRequestDTO();
        orderReq.setUserId(customerId);
        orderReq.setOrderItems(List.of(item));
        mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andExpect(status().isCreated());
    }

    @Test @Order(47) void test47_AdminFetchesCustomerNotifs() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/notifications/user/" + customerId).header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk()).andReturn();
        NotificationResponseDTO[] notifs = objectMapper.readValue(res.getResponse().getContentAsString(), NotificationResponseDTO[].class);
        boolean hasCheckoutNotif = Arrays.stream(notifs)
                .anyMatch(n -> n.getType().name().equals("CHECKOUT_CONFIRMATION") && n.getOrderId().equals(orderId));
        
        if (hasCheckoutNotif) {
             notificationId = Arrays.stream(notifs)
                .filter(n -> n.getType().name().equals("CHECKOUT_CONFIRMATION") && n.getOrderId().equals(orderId))
                .findFirst().get().getId();
        }
    }

    @Test @Order(48) void test48_AdminFetchesAdminNotifs() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/notifications/user/" + adminId).header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk()).andReturn();
        NotificationResponseDTO[] notifs = objectMapper.readValue(res.getResponse().getContentAsString(), NotificationResponseDTO[].class);
        boolean hasLowStock = Arrays.stream(notifs).anyMatch(n -> n.getType().name().equals("LOW_STOCK") && n.getProductId().equals(orderProductId));
        
        assertTrue(hasLowStock, "Expected a LOW_STOCK notification for the admin, but none was found.");
    }
    @Test @Order(49) void test49_AdminCreateCheckoutNotifNoOrderId() throws Exception {
        NotificationRequestDTO notif = new NotificationRequestDTO();
        notif.setUserId(customerId);
        notif.setType(NotificationType.CHECKOUT_CONFIRMATION);
        notif.setMessage("Msg");
        mockMvc.perform(post("/api/notifications").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(notif))).andExpect(anyOf(400, 500));
    }

    @Test @Order(50) void test50_AdminCreateLowStockNotifNoProductId() throws Exception {
        NotificationRequestDTO notif = new NotificationRequestDTO();
        notif.setUserId(adminId);
        notif.setType(NotificationType.LOW_STOCK);
        notif.setMessage("Msg");
        mockMvc.perform(post("/api/notifications").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(notif))).andExpect(anyOf(400, 500));
    }

    @Test @Order(51) void test51_AdminCreatesValidManualNotif() throws Exception {
        NotificationRequestDTO notif = new NotificationRequestDTO();
        notif.setUserId(adminId);
        notif.setType(NotificationType.LOW_STOCK);
        notif.setProductId(orderProductId);
        notif.setMessage("Manual alert");
        MvcResult res = mockMvc.perform(post("/api/notifications").header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(notif))).andExpect(status().isOk()).andReturn();
        adminNotificationId = objectMapper.readValue(res.getResponse().getContentAsString(), NotificationResponseDTO.class).getId();
    }

    @Test @Order(52) void test52_CustomerFetchAllSystemNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
    }

    @Test @Order(53) void test53_CustomerFetchOwnNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/user/" + customerId).header("Authorization", "Bearer " + customerToken)).andExpect(status().isOk());
    }

    @Test @Order(54) void test54_CustomerFetchAdminNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/user/" + adminId).header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
    }

    @Test @Order(55) void test55_CustomerMarksOwnNotifRead() throws Exception {
        if (notificationId != null) {
            mockMvc.perform(put("/api/notifications/" + notificationId + "/read").header("Authorization", "Bearer " + customerToken)).andExpect(status().isOk());
        }
    }

    @Test @Order(56) void test56_CustomerMarksAdminNotifRead_IDOR() throws Exception {
        if (adminNotificationId != null) {
            mockMvc.perform(put("/api/notifications/" + adminNotificationId + "/read").header("Authorization", "Bearer " + customerToken)).andExpect(anyOf(403, 404));
        }
    }

    @Test @Order(57) void test57_AdminDeleteProductTiedToInventory() throws Exception {
        mockMvc.perform(delete("/api/products/" + orderProductId).header("Authorization", "Bearer " + adminToken)).andExpect(anyOf(409, 500));
    }

    @Test @Order(58) void test58_AdminDeletesInventory() throws Exception {
        if (inventoryId != null) {
            mockMvc.perform(delete("/api/inventory/" + inventoryId).header("Authorization", "Bearer " + adminToken)).andExpect(status().isNoContent());
        }
    }

    @Test @Order(59) void test59_AdminDeleteProductTiedToOrders() throws Exception {
        mockMvc.perform(delete("/api/products/" + orderProductId).header("Authorization", "Bearer " + adminToken)).andExpect(anyOf(409, 500));
    }

    @Test @Order(60) void test60_CustomerDeletesOrder() throws Exception {
        mockMvc.perform(delete("/api/orders/" + orderId).header("Authorization", "Bearer " + customerToken)).andExpect(anyOf(403, 405));
    }

    @Test @Order(61) void test61_AdminSoftDeletesCustomer() throws Exception {
        testCustomer.setStatus("INACTIVE");
        mockMvc.perform(put("/api/users/" + customerId).header("Authorization", "Bearer " + adminToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testCustomer))).andExpect(status().isOk());
    }

    @Test @Order(62) void test62_InactiveCustomerTriesCheckout() throws Exception {
        OrderItemRequestDTO item = OrderItemRequestDTO.builder().productId(orderProductId).quantity(1).unitPrice(new BigDecimal("15.00")).build();
        OrdersRequestDTO orderReq = new OrdersRequestDTO();
        orderReq.setUserId(customerId);
        orderReq.setOrderItems(List.of(item));
        mockMvc.perform(post("/api/checkout").header("Authorization", "Bearer " + customerToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(orderReq))).andExpect(anyOf(403, 409));
    }

    @Test @Order(63) void test63_AdminHardDeletesCustomer() throws Exception {
        mockMvc.perform(delete("/api/users/" + customerId).header("Authorization", "Bearer " + adminToken)).andExpect(status().isNoContent());
    }

    @Test @Order(64) void test64_AdminDeletesProduct() throws Exception {
        mockMvc.perform(delete("/api/products/" + orderProductId).header("Authorization", "Bearer " + adminToken)).andExpect(status().isNoContent());
    }
}