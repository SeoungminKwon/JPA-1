package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출
     * 엔티티가 변하면 API 스펙이 변함
     * 트렌젝션 안에서 지연로딩 필요 -> Hibernate5Module 등록, Lazy = null 처리
     * 양방향 연관관계 문제 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List< Order > ordersV1() {
        List< Order > all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // LAZT 강제 초기화
            order.getDelivery().getAddress(); // LAZT 강제 초기화
            List< OrderItem > orderItems = order.getOrderItems();
            orderItems.stream()
                    .forEach(o -> o.getItem().getName()); // LAZT 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List< OrderDto > ordersV2() {
        List< Order > all = orderRepository.findAllByString(new OrderSearch());
        List< OrderDto > collect = all.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return collect;
    }


    @Getter
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List< OrderItemDto > orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }

    @GetMapping("/api/v3/orders")
    public List< OrderDto > ordersV3() {
        List< Order > orders = orderRepository.findWithItem();
        List< OrderDto > result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ){
        // ???ToOne 연관관계만 fetch join해서 페이징 처리
        List< Order > orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List< OrderDto > result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }
}
