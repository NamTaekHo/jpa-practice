package com.springboot.order.entity;

import com.springboot.coffee.entity.Coffee;
import com.springboot.config.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class OrderCoffee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderCoffeeId;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "coffee_id")
    private Coffee coffee;

    public void setOrder(Order order){
        this.order = order;
        if(!order.getOrderCoffees().contains(this)){
            order.getOrderCoffees().add(this);
        }
    }
}
