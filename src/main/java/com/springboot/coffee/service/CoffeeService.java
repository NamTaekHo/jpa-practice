package com.springboot.coffee.service;

import com.springboot.coffee.entity.Coffee;
import com.springboot.coffee.repository.CoffeeRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CoffeeService {
    private final CoffeeRepository coffeeRepository;

    public CoffeeService(CoffeeRepository coffeeRepository) {
        this.coffeeRepository = coffeeRepository;
    }

    public Coffee createCoffee(Coffee coffee){
        // 입력받은 커피코드를 대문자로 변경해서 DB에 저장해야함
        String coffeeCode = coffee.getCoffeeCode().toUpperCase();
        // DB에 등록된 커피코드인지 확인
        verifyExistCoffeeCode(coffeeCode);
        // 올바른 커피코드면 엔티티에 대문자로 바꾼 커피코드 set
        coffee.setCoffeeCode(coffeeCode);
        return coffeeRepository.save(coffee);
    }

    // Coffee Update 메서드
    public Coffee updateCoffee(Coffee coffee) {
        // coffeeId로 커피 찾고
        Coffee findCoffee = findVerifiedCoffee(coffee.getCoffeeId());
        // 수정가능한 필드들 ofNullable사용해서 Null이면 원래값, Null이 아니면 들어온 값으로 수정 (korName, engName, price, coffeeStatus)
        Optional.ofNullable(coffee.getKorName())
                .ifPresent(korName -> findCoffee.setKorName(korName));
        Optional.ofNullable(coffee.getEngName())
                .ifPresent(engName -> findCoffee.setEngName(engName));
        Optional.ofNullable(coffee.getPrice())
                .ifPresent(price -> findCoffee.setPrice(price));
        Optional.ofNullable(coffee.getCoffeeStatus())
                .ifPresent(coffeeStatus -> findCoffee.setCoffeeStatus(coffeeStatus));

        // 수정 후 저장
        return coffeeRepository.save(findCoffee);
    }

    // 커피 단일 조회
    public Coffee findCoffee(long coffeeId){
        return findVerifiedCoffee(coffeeId);
    }

    // 커피 전체 조회
    public Page<Coffee> findCoffees(int page, int size){
        return coffeeRepository.findAll(PageRequest.of(page-1, size, Sort.by("coffeeId").descending()));
    }

    // 커피 삭제(DB에서 삭제하면 안되고 상태만 바꿈)
    public void deleteCoffee(long coffeeId){
        Coffee coffee = findVerifiedCoffee(coffeeId);
        coffee.setCoffeeStatus(Coffee.CoffeeStatus.COFFEE_SOLD_OUT);
        coffeeRepository.save(coffee);
    }

    // DB에 같은 커피코드 존재하는지 확인하는 메서드
    private void verifyExistCoffeeCode(String coffeeCode) {
        // DB에서 커피코드 존재하는지 확인하고 없으면 예외처리
        coffeeRepository.findByCoffeeCode(coffeeCode).orElseThrow(() -> new BusinessLogicException(ExceptionCode.COFFEE_CODE_EXISTS));
    }

    // coffeeId가 존재하는지 확인 후 존재하면 반환, 존재하지 않으면 예외발생
    private Coffee findVerifiedCoffee(long coffeeId) {
        // 커피아이디로 커피 찾기
        Optional<Coffee> optionalCoffee = coffeeRepository.findByCoffeeId(coffeeId);

        // 없으면 예외 발생시키고 있으면 Coffee 반환
        return optionalCoffee.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.COFFEE_NOT_FOUND));
    }
}
