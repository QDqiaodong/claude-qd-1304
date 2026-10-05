package com.maternal.care.meal;

import com.maternal.care.common.BizException;
import com.maternal.care.common.Dict;
import com.maternal.care.maternal.Maternal;
import com.maternal.care.maternal.MaternalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final MaternalRepository maternalRepository;

    public MealService(MealRepository mealRepository, MaternalRepository maternalRepository) {
        this.mealRepository = mealRepository;
        this.maternalRepository = maternalRepository;
    }

    public List<Meal> list(LocalDate serveDate) {
        if (serveDate == null) {
            return mealRepository.findAllByOrderByServeDateDescIdDesc();
        }
        return mealRepository.findByServeDateOrderByIdAsc(serveDate);
    }

    public Meal create(Meal input) {
        Maternal maternal = residentMaternal(input.getMaternalId());
        validateMealName(input.getMealName());
        if (!Dict.isValidMealType(input.getMealType())) {
            throw new BizException("餐别必须是：早餐、午餐、晚餐或加餐");
        }
        validateCalories(input.getCalories());
        if (input.getStatus() != null && !input.getStatus().isBlank()
                && !Dict.MEAL_PENDING.equals(input.getStatus())) {
            throw new BizException("新配餐状态只能为待配送");
        }

        Meal meal = new Meal();
        meal.setMaternalId(maternal.getId());
        meal.setMealName(input.getMealName().trim());
        meal.setMealType(input.getMealType());
        meal.setServeDate(input.getServeDate() == null ? LocalDate.now() : input.getServeDate());
        meal.setCalories(input.getCalories());
        meal.setStatus(Dict.MEAL_PENDING);
        meal.setCreatedAt(LocalDateTime.now());
        meal.setUpdatedAt(LocalDateTime.now());
        return mealRepository.save(meal);
    }

    public Meal update(Long id, Meal input) {
        Meal meal = get(id);
        if (Dict.MEAL_DELIVERED.equals(meal.getStatus())) {
            throw new BizException("已配送的月子餐不可再修改");
        }
        if (Dict.MEAL_CANCELLED.equals(meal.getStatus())) {
            throw new BizException("已取消的月子餐不可再修改");
        }
        if (input.getMaternalId() != null && !input.getMaternalId().equals(meal.getMaternalId())) {
            Maternal maternal = residentMaternal(input.getMaternalId());
            meal.setMaternalId(maternal.getId());
        }
        if (input.getMealName() != null) {
            validateMealName(input.getMealName());
            meal.setMealName(input.getMealName().trim());
        }
        if (input.getMealType() != null) {
            if (!Dict.isValidMealType(input.getMealType())) {
                throw new BizException("餐别必须是：早餐、午餐、晚餐或加餐");
            }
            meal.setMealType(input.getMealType());
        }
        if (input.getServeDate() != null) {
            meal.setServeDate(input.getServeDate());
        }
        if (input.getCalories() != null) {
            validateCalories(input.getCalories());
            meal.setCalories(input.getCalories());
        }
        if (input.getStatus() != null && !input.getStatus().equals(meal.getStatus())) {
            throw new BizException("请使用配送或取消操作变更月子餐状态");
        }
        return mealRepository.save(meal);
    }

    public Meal deliver(Long id) {
        Meal meal = get(id);
        if (Dict.MEAL_CANCELLED.equals(meal.getStatus())) {
            throw new BizException("已取消的月子餐不可配送");
        }
        if (Dict.MEAL_DELIVERED.equals(meal.getStatus())) {
            throw new BizException("月子餐已配送，不可重复操作");
        }
        meal.setStatus(Dict.MEAL_DELIVERED);
        return mealRepository.save(meal);
    }

    public Meal cancel(Long id) {
        Meal meal = get(id);
        if (Dict.MEAL_DELIVERED.equals(meal.getStatus())) {
            throw new BizException("已配送的月子餐不可取消");
        }
        if (Dict.MEAL_CANCELLED.equals(meal.getStatus())) {
            throw new BizException("月子餐已取消，不可重复操作");
        }
        meal.setStatus(Dict.MEAL_CANCELLED);
        return mealRepository.save(meal);
    }

    private Meal get(Long id) {
        return mealRepository.findById(id).orElseThrow(() -> new BizException("月子餐记录不存在"));
    }

    private Maternal residentMaternal(Long maternalId) {
        if (maternalId == null) {
            throw new BizException("必须选择产妇");
        }
        Maternal maternal = maternalRepository.findById(maternalId)
                .orElseThrow(() -> new BizException("产妇档案不存在"));
        if (!Dict.MATERNAL_RESIDENT.equals(maternal.getStatus())) {
            throw new BizException("只能给在住产妇安排月子餐");
        }
        return maternal;
    }

    private void validateMealName(String mealName) {
        if (mealName == null || mealName.isBlank()) {
            throw new BizException("餐名不能为空");
        }
    }

    private void validateCalories(Integer calories) {
        if (calories == null || calories <= 0) {
            throw new BizException("餐食热量必须大于0千卡");
        }
    }
}
