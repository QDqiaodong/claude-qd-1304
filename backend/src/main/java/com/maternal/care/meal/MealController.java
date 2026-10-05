package com.maternal.care.meal;

import com.maternal.care.common.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    public ApiResponse<List<Meal>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate serveDate) {
        return ApiResponse.success(mealService.list(serveDate));
    }

    @PostMapping
    public ApiResponse<Meal> create(@RequestBody Meal input) {
        return ApiResponse.success(mealService.create(input));
    }

    @PutMapping("/{id}")
    public ApiResponse<Meal> update(@PathVariable Long id, @RequestBody Meal input) {
        return ApiResponse.success(mealService.update(id, input));
    }

    @PostMapping("/{id}/deliver")
    public ApiResponse<Meal> deliver(@PathVariable Long id) {
        return ApiResponse.success(mealService.deliver(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Meal> cancel(@PathVariable Long id) {
        return ApiResponse.success(mealService.cancel(id));
    }
}
