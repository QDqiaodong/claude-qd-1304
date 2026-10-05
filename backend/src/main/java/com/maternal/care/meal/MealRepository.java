package com.maternal.care.meal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findAllByOrderByServeDateDescIdDesc();

    List<Meal> findByServeDateOrderByIdAsc(LocalDate serveDate);
}
