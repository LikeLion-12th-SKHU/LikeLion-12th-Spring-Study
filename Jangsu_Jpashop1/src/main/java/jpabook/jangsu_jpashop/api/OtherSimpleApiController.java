package jpabook.jangsu_jpashop.api;

import jpabook.jangsu_jpashop.domain.Address;
import jpabook.jangsu_jpashop.domain.Order;
import jpabook.jangsu_jpashop.domain.OrderStatus;
import jpabook.jangsu_jpashop.repository.OrderRepository;
import jpabook.jangsu_jpashop.repository.OrderSearch;
import jpabook.jangsu_jpashop.repository.simplequery.OrderSimpleQueryDto;
import jpabook.jangsu_jpashop.repository.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//---ToOne(ManyToOne / OneToOne) 관계 최적화
// Order -> Member, Order -> Delivery
@RestController
@RequiredArgsConstructor
public class OtherSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // 엔티티 직접 노출 - 권장하지 않는다. 추후에 유지보수하기 까다로워진다.
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for(Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all;
    }

    // 엔티티를 DTO로 변환1 - 일반적인 방법, 권장하지만 쿼리가 N번 호출된다.
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o->new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getDelivery().getAddress();
        }
    }

    // 엔티티를 DTO로 변환2 - 일반적인 방법, 페치 / 조인에 최적화됨.
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    // JPA에서 DTO로 바로 조회 - 쿼리를 1번 호출한다.
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDto();
    }

    /*
        ※ 쿼리 방식 권장 순서
        1. 엔티티를 DTO로 변환하는 방법을 선택한다.
        2. 필요하면 패치 - 조인 방식으로 성능을 최적화한다.
        3. 안되면 DTO로 직접 조회하는 방법을 사용한다.
        4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용하여 SQL을 직접 사용한다.
    */

}
