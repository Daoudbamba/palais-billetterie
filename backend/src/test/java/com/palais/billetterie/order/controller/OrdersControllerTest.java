package com.palais.billetterie.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palais.billetterie.common.exceptions.AppExceptionHandler;
import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.domain.OrderStatus;
import com.palais.billetterie.order.dto.CreateOrderRequest;
import com.palais.billetterie.order.dto.UpdateOrderRequest;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.event.repository.EventRepository;
import com.palais.billetterie.user.domain.Role;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import com.palais.billetterie.security.jwt.JwtService;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrdersController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AppExceptionHandler.class)
class OrdersControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderRepository orderRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    EventRepository eventRepository;

    @MockBean
    JwtService jwtService;

    @MockBean
    DataSourceProperties dataSourceProperties;

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void createOrder_ok() throws Exception {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder().id(eventId).capacity(100).build();
        Mockito.when(eventRepository.findById(eq(eventId))).thenReturn(Optional.of(event));

        User user = User.builder().id(UUID.randomUUID()).email("user@example.com").name("Test").role(Role.USER).build();
        Mockito.when(userRepository.findByEmail(eq("user@example.com"))).thenReturn(Optional.of(user));

        Mockito.when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            return Order.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .event(event)
                    .amount(10.0 * (o.getQuantity() == null ? 1 : o.getQuantity()))
                    .quantity(o.getQuantity())
                    .status(OrderStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();
        });

        CreateOrderRequest req = CreateOrderRequest.builder().eventId(eventId).quantity(2).build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.amount").value(20.0))
            .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void createOrder_invalidQuantity_returns400() throws Exception {
        UUID eventId = UUID.randomUUID();
        CreateOrderRequest req = CreateOrderRequest.builder().eventId(eventId).quantity(0).build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void updateOrder_ok() throws Exception {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder().id(eventId).capacity(100).build();
        User user = User.builder().id(UUID.randomUUID()).email("user@example.com").name("Test").role(Role.USER).build();

        UUID orderId = UUID.randomUUID();
        Order existing = Order.builder()
                .id(orderId)
                .user(user)
                .event(event)
                .amount(10.0)
                .quantity(1)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        Mockito.when(orderRepository.findById(eq(orderId))).thenReturn(Optional.of(existing));

        Mockito.when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateOrderRequest req = UpdateOrderRequest.builder().quantity(3).build();

        mockMvc.perform(patch("/api/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderId.toString()))
            .andExpect(jsonPath("$.quantity").value(3))
            .andExpect(jsonPath("$.amount").value(30.0));
    }
}
