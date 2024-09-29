package org.example.jpashop.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.jpashop.domain.item.Item;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    @Column(name = "order_item_id")
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "order_Id")
    private Order order;

    private int orderPrice;
    private int count;

    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    public void cancel() {
        getItem().addStock(count);
    }

    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
