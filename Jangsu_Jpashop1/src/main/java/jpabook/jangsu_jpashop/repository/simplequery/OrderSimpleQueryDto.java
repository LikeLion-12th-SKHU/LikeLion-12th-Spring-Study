package jpabook.jangsu_jpashop.repository.simplequery;

import jpabook.jangsu_jpashop.domain.Address;
import jpabook.jangsu_jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    private OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate,
                           OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
