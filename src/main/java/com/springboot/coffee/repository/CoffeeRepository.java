package com.springboot.coffee.repository;

import com.springboot.coffee.entity.Coffee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoffeeRepository extends JpaRepository<Coffee, Long> {
    // 커피코드로 커피 찾는 메서드
    Optional<Coffee> findByCoffeeCode(String coffeeCode);
    // 커피아이디로 커피 찾는 메서드
    Optional<Coffee> findByCoffeeId(long coffeeId);
}
