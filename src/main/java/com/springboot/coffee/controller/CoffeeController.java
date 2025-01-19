package com.springboot.coffee.controller;

import com.springboot.coffee.dto.CoffeePatchDto;
import com.springboot.coffee.dto.CoffeePostDto;
import com.springboot.coffee.entity.Coffee;
import com.springboot.coffee.mapper.CoffeeMapper;
import com.springboot.coffee.service.CoffeeService;
import com.springboot.response.MultiResponseDto;
import com.springboot.response.SingleResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/v12/coffees")
@Validated
public class CoffeeController {
    private CoffeeService coffeeService;
    private CoffeeMapper mapper;

    public CoffeeController(CoffeeService coffeeService, CoffeeMapper mapper) {
        this.coffeeService = coffeeService;
        this.mapper = mapper;
    }

    // 커피 등록
    @PostMapping
    public ResponseEntity postCoffee(@Valid @RequestBody CoffeePostDto coffeePostDto){
        // mapper로 PostDto -> Entity로 바꾼 후 DB에 저장
        Coffee coffee = coffeeService.createCoffee(mapper.coffeePostDtoToCoffee(coffeePostDto));
        // CREATED 코드와 함께 리턴
        return new ResponseEntity(mapper.coffeeToCoffeeResponseDto(coffee), HttpStatus.CREATED);
    }

    // 커피 수정
    @PatchMapping("/{coffee-id}")
    public ResponseEntity patchCoffee(@PathVariable("coffee-id") @Positive long coffeeId,
                                      @Valid @RequestBody CoffeePatchDto coffeePatchDto) {
        // PatchDto에 coffeeId set
        coffeePatchDto.setCoffeeId(coffeeId);
        // PatchDto -> Entity 변환(updateCoffee로 DB에 저장)
        Coffee coffee = coffeeService.updateCoffee(mapper.coffeePatchDtoToCoffee(coffeePatchDto));
        // Entity -> ResponseDto 변환 후 리턴
        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.coffeeToCoffeeResponseDto(coffee)),
                HttpStatus.OK);
    }

    // 단일 조회
    @GetMapping("/{coffee-id}")
    public ResponseEntity getCoffee(@PathVariable("coffee-id") long coffeeId) {
        // coffeeId로 해당 객체 찾고
        Coffee coffee = coffeeService.findCoffee(coffeeId);
        // ResponseDto로 변환 후 반환
        return new ResponseEntity<>(
                new SingleResponseDto<>(mapper.coffeeToCoffeeResponseDto(coffee)),
                HttpStatus.OK);
    }

    // 커피 전체 조회
    @GetMapping
    public ResponseEntity getCoffees(@Positive @RequestParam int page,
                                     @Positive @RequestParam int size) {
        // 커피 전체 찾아서 페이지네이션
        Page<Coffee> coffeePage = coffeeService.findCoffees(page, size);
        // mapper 쓰기 위해 List로 변환
        List<Coffee> coffees = coffeePage.getContent();
        // ResponseDto에 List와 Page 객체 넣어서 반환
        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.coffeesToCoffeeResponseDtos(coffees),
                        coffeePage),
                HttpStatus.OK);
    }

    @DeleteMapping("/{coffee-id}")
    public ResponseEntity deleteCoffee(@PathVariable("coffee-id") long coffeeId) {
        // 커피 아이디로 delete메서드 실행
        coffeeService.deleteCoffee(coffeeId);
        // 결과 반환
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
