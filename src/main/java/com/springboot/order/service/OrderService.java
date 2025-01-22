package com.springboot.order.service;

import com.springboot.coffee.entity.Coffee;
import com.springboot.coffee.service.CoffeeService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.order.entity.Order;
import com.springboot.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderService {
    // Order의 필드에 Member와 OrderCoffee가 있기 때문에 검증을 위해 서비스 주입
    private MemberService memberService;
    private CoffeeService coffeeService;
    private OrderRepository orderRepository;

    public OrderService(MemberService memberService, CoffeeService coffeeService, OrderRepository orderRepository) {
        this.memberService = memberService;
        this.coffeeService = coffeeService;
        this.orderRepository = orderRepository;
    }

    // 주문 생성
    public Order createOrder(Order order){
        // 컨트롤러에서 PostDto를 변환한 Entity를 받아서 검증 후 저장
        // 멤버가 존재하는지 검증하면서 스탬프 업데이트
        Member member = memberService.findVerifiedMember(order.getMember().getMemberId());
        // List<OrderCoffee> 순회하면서 각각의 커피코드가 존재하는지 검증
        order.getOrderCoffees().stream()
                .forEach(orderCoffee ->
                        {coffeeService.verifyExistCoffeeCode(orderCoffee.getCoffee().getCoffeeCode());
                        Coffee coffee = coffeeService.findCoffee(orderCoffee.getCoffee().getCoffeeId());
                        orderCoffee.setCoffee(coffee);
                        });

        // 추가할 스탬프 개수
        int addStamp = order.getOrderCoffees().stream().mapToInt(orderCoffee -> orderCoffee.getQuantity()).sum();
        member.getStamp().setStampCount(addStamp);
        memberService.updateMember(member);
        // 검증이 모두 끝났으면 DB에 저장 후 반환
        return orderRepository.save(order);
    }

    // 주문 수정
    public Order updateOrder(Order order){
    // patchDto -> Entity로 변환한 order 받아서 orderId로 주문이 있는지 확인하고
        // 수정할 수 있는 부분이 status 밖에 없기 때문에 status가 바뀌었는지 확인 후 저장, 반환
        Order foundOrder = verifyExistsOrder(order.getOrderId());
        // status 확인
        Optional.ofNullable(order.getOrderStatus())
                .ifPresent(orderStatus -> foundOrder.setOrderStatus(order.getOrderStatus()));
        // 수정 후 저장, 반환
        return orderRepository.save(foundOrder);
    }

    // 주문 단건 조회
    public Order findOrder(long orderId){
        return verifyExistsOrder(orderId);
    }

    // 주문 전체 조회(페이지네이션)
    public Page<Order> findOrders(int page, int size){
        Page<Order> orders = orderRepository.findAll(
                PageRequest.of(page-1, size, Sort.by("orderId").descending())
        );
        return orders;
    }

    // 주문 삭제
    public void deleteOrder(long orderId){
        // 상태만 변경하고 DB에서 삭제 x
        Order foundOrder = verifyExistsOrder(orderId);
        foundOrder.setOrderStatus(Order.OrderStatus.ORDER_CANCEL);
        orderRepository.save(foundOrder);
    }

    // 아이디로 주문이 존재하는지 검증하는 메서드
    private Order verifyExistsOrder(long orderId){
        return orderRepository.findById(orderId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND)
        );
    }
}
