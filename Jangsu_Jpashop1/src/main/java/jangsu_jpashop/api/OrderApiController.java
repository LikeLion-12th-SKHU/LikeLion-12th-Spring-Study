package jangsu_jpashop.api;

import jangsu_jpashop.domain.Address;
import jangsu_jpashop.domain.Order;
import jangsu_jpashop.domain.OrderItem;
import jangsu_jpashop.domain.OrderStatus;
import jangsu_jpashop.repository.order.query.OrderFlatDto;
import jangsu_jpashop.repository.order.query.OrderItemQueryDto;
import jangsu_jpashop.repository.order.query.OrderQueryDto;
import jangsu_jpashop.repository.order.query.OrderQueryRepository;
import jangsu_jpashop.repository.OrderRepository;
import jangsu_jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository; // V4

    // 방법 1. 엔티티 직접 노출
    // 양방향 연관관계이면 @JsonIgnore를 추가하여 무한루프를 방지한다.
    // 엔티티를 직접 노출하므로 좋은 방법은 아니다.
    @GetMapping("api/v1/orders")
    public List<Order> orderV1() {
        List<Order> findAll = orderRepository.findAllByString(new OrderSearch());
        for (Order order : findAll) {
            order.getMember().getName();                                // Lazy 강제 초기화
            order.getDelivery().getAddress();                           // Lazy 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());    // Lazy 강제 초기화
        }

        return findAll;
    }

    // 방법 2. 엔티티를 DTO로 변환
    // 엔티티를 필요한 만큼 사용하므로 윗 방법보다는 낫다.
    // 그러나 지연로딩으로 인해 SQL을 많이 사용하므로 성능을 요구하는 곳은 적절하지 않다.
    @GetMapping("api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    // 방법 3. 엔티티를 DTO로 변환 - 페치 조인 최적화
    // 페치 조인으로 SQL이 한번만 실행된다. - 장점
    // 컬렉션을 페치-조인하면 페이징 불가, 데이터가 예측할 수 없이 증가 -> 데이터 양이 많으면 장애를 우려함.
    @GetMapping("api/v3/orders")
    public List<OrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o-> new OrderDto(o))
                .collect(toList());

        return result;
    }

    // 방법 3.1. 엔티티를 DTO로 변환 - 페이징과 한계 돌파
    // 컬렉션을 페치 조인하면 일대다 조인이 발생하여 데이터가 예측할 수 없이 증가하는데, 이는 장애를 우려한다.
    // 이를 ToOne 관계를 모두 페치-조인하고 지연로딩, 페치 사이즈를 제한하여 최적화를 함.
    @GetMapping("api/v3.1/orders")
    public List<OrderDto> orderV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                       @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    // 방법 4. JPA에서 DTO 직접 조회
    @GetMapping("api/v4/orders")
    public List<OrderQueryDto> orderV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    // 방법 5. JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
    @GetMapping("api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepository.findAllByDto_Optimization();
    }

    // 방법 6. JPA에서 DTO로 직접 조회 - 플랫 데이터 최적화
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count;      //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    /*
    ※ 권장하는 조회 방법
        1. 엔티티 조회 방식으로 우선 접근 - 방법 3. 사용
            1. 페치조인으로 쿼리 수를 최적화
            2. 컬렉션 최적화
                1. 페이징 필요 hibernate.default_batch_fetch_size , @BatchSize 로 최적화
                2. 페이징 필요X 페치 조인 사용
        2. 엔티티 조회 방식으로 해결이 안되면 DTO 조회 방식 사용 - 방법 2. 사용
        3. DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate
     */
}
