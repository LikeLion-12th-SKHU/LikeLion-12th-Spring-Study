package org.likelion.spring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    // 양방향 걸리는 곳 중 하나에 JsonIgnore
    @JsonIgnore
    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING) // 중간에 다른게 들어가도 밀리는 게 없음 -> ordinary xx
    private DeliveryStatus status; // READY, COMP
}
