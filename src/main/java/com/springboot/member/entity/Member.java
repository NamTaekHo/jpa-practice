package com.springboot.member.entity;

import com.springboot.config.BaseEntity;
import com.springboot.order.entity.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, updatable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 13, nullable = false, unique = true)
    private String phone;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stamp_id")
    private Stamp stamp;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    public void setOrders(Order order){
        if(order.getMember() != this){
            order.setMember(this);
        }
        this.orders.add(order);
        this.getStamp().setStampCount(
                order.getOrderCoffees().stream().mapToInt(orderCoffee -> orderCoffee.getQuantity()).sum()
        );
    }

    public Member(String email){
        this.email = email;
    }

    public Member(String email, String name, String phone){
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    public enum MemberStatus{
        MEMBER_ACTIVE("활동중"),
        MEMBER_SLEEP("휴면 상태"),
        MEMBER_QUIT("탈퇴 상태");

        @Getter
        private String status;

        MemberStatus(String status){
            this.status = status;
        }
    }

//    @Column(nullable = false)
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    @Column(nullable = false, name = "LAST_MODIFIED_AT")
//    private LocalDateTime modifiedAt = LocalDateTime.now();

}
