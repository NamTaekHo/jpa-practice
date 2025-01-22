package com.springboot.order.controller;

import com.springboot.member.service.MemberService;
import com.springboot.order.dto.OrderPatchDto;
import com.springboot.order.dto.OrderPostDto;
import com.springboot.order.entity.Order;
import com.springboot.order.mapper.OrderMapper;
import com.springboot.order.service.OrderService;
import com.springboot.response.MultiResponseDto;
import com.springboot.response.SingleResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/v12/orders")
@Validated
public class OrderController {
    private OrderService orderService;
    private OrderMapper orderMapper;
    private MemberService memberService;

    public OrderController(OrderService orderService, OrderMapper orderMapper, MemberService memberService) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity postOrder(@Valid @RequestBody OrderPostDto dto){
        Order order = orderService.createOrder(orderMapper.orderPostDtoToOrder(dto));
        return new ResponseEntity(new SingleResponseDto<>(orderMapper.orderToOrderResponseDto(order)), HttpStatus.CREATED);
    }

    @PatchMapping("/{order-id}")
    public ResponseEntity patchOrder(@Valid @RequestBody OrderPatchDto dto){
        Order order = orderService.updateOrder(orderMapper.orderPatchDtoToOrder(dto));
        return new ResponseEntity(new SingleResponseDto<>(orderMapper.orderToOrderResponseDto(order)), HttpStatus.OK);
    }

    @GetMapping("/{order-id}")
    public ResponseEntity getOrder(@Positive @PathVariable("/{order-id}") long orderId){
        Order order = orderService.findOrder(orderId);
        return new ResponseEntity(new SingleResponseDto<>(orderMapper.orderToOrderResponseDto(order)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getOrders(@Positive @RequestParam int page,
                                    @Positive @RequestParam int size){
        Page<Order> orderPage = orderService.findOrders(page, size);
        List<Order> orders = orderPage.getContent();

        return new ResponseEntity(new MultiResponseDto<>(orderMapper.ordersToOrderResponseDtos(orders), orderPage), HttpStatus.OK);
    }

    @DeleteMapping("/{order-id}")
    public ResponseEntity cancelOrder(@Positive @PathVariable("order-id") long orderId){
        orderService.deleteOrder(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
