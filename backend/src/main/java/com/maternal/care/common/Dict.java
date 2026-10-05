package com.maternal.care.common;

import java.util.List;

/** 产后护理中心统一业务字典。 */
public final class Dict {

    private Dict() {
    }

    public static final List<String> ROOM_TYPES = List.of("标准间", "单间", "套房");
    public static final String ROOM_AVAILABLE = "空闲";
    public static final String ROOM_OCCUPIED = "在住";
    public static final String ROOM_CLEANING = "清洁中";
    public static final String ROOM_MAINTENANCE = "维修";
    public static final List<String> ROOM_STATUS = List.of(
            ROOM_AVAILABLE, ROOM_OCCUPIED, ROOM_CLEANING, ROOM_MAINTENANCE);

    public static final String MATERNAL_PENDING = "待入住";
    public static final String MATERNAL_RESIDENT = "在住";
    public static final String MATERNAL_DISCHARGED = "已出院";
    public static final String MATERNAL_WITHDRAWN = "退住";
    public static final List<String> MATERNAL_STATUS = List.of(
            MATERNAL_PENDING, MATERNAL_RESIDENT, MATERNAL_DISCHARGED, MATERNAL_WITHDRAWN);

    public static final List<String> CARE_TYPES = List.of(
            "伤口护理", "乳房护理", "新生儿护理", "产后恢复", "心理疏导");
    public static final String CARE_PENDING = "待执行";
    public static final String CARE_COMPLETED = "已完成";
    public static final String CARE_SKIPPED = "已跳过";
    public static final List<String> CARE_STATUS = List.of(
            CARE_PENDING, CARE_COMPLETED, CARE_SKIPPED);

    public static final List<String> MEAL_TYPES = List.of("早餐", "午餐", "晚餐", "加餐");
    public static final String MEAL_PENDING = "待配送";
    public static final String MEAL_DELIVERED = "已配送";
    public static final String MEAL_CANCELLED = "已取消";
    public static final List<String> MEAL_STATUS = List.of(
            MEAL_PENDING, MEAL_DELIVERED, MEAL_CANCELLED);

    public static boolean isValidRoomType(String type) {
        return type != null && ROOM_TYPES.contains(type);
    }

    public static boolean isValidRoomStatus(String status) {
        return status != null && ROOM_STATUS.contains(status);
    }

    public static boolean isValidMaternalStatus(String status) {
        return status != null && MATERNAL_STATUS.contains(status);
    }

    public static boolean isValidCareType(String careType) {
        return careType != null && CARE_TYPES.contains(careType);
    }

    public static boolean isValidCareStatus(String status) {
        return status != null && CARE_STATUS.contains(status);
    }

    public static boolean isValidMealType(String mealType) {
        return mealType != null && MEAL_TYPES.contains(mealType);
    }

    public static boolean isValidMealStatus(String status) {
        return status != null && MEAL_STATUS.contains(status);
    }
}
