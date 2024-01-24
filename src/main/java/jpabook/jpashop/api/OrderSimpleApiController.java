package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.api.OrderSimpleQueryDto;
import jpabook.jpashop.repository.api.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * 엔티티 직접 노출
     */
    @GetMapping("/api/v1/simple-orders")
    public List< Order > ordersV1() {
        List< Order > all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }


    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용 x)
     */
    @GetMapping("/api/v2/simple-orders")
    public List< SimpleOrderDto > ordersV2() {
        List< Order > orders = orderRepository.findAllByCriteria(new OrderSearch());
        List< SimpleOrderDto > result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; // 주문 시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }


    /**
     * V3. 엔티티를 조회해서 DTO로 변환 (fetch join 사용 O)
     */

    @GetMapping("/api/v3/simple-orders")
    public List< SimpleOrderDto > ordersV3() {
        List< Order > orders = orderRepository.findAllWithMemberDelivery();
        List< SimpleOrderDto > result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @GetMapping("/api/v4/simple-orders")
    public List< OrderSimpleQueryDto > ordersV4() {
        List< OrderSimpleQueryDto > orderDtos = orderSimpleQueryRepository.findOrderDtos();
        return orderDtos;
    }
}
