package com.maternal.care.care;

import com.maternal.care.common.BizException;
import com.maternal.care.common.Dict;
import com.maternal.care.maternal.Maternal;
import com.maternal.care.maternal.MaternalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CareService {

    private final CareRepository careRepository;
    private final MaternalRepository maternalRepository;

    public CareService(CareRepository careRepository, MaternalRepository maternalRepository) {
        this.careRepository = careRepository;
        this.maternalRepository = maternalRepository;
    }

    public List<CareTask> list(LocalDate planDate) {
        if (planDate == null) {
            return careRepository.findAllByOrderByPlanDateDescIdDesc();
        }
        return careRepository.findByPlanDateOrderByIdAsc(planDate);
    }

    public CareTask create(CareTask input) {
        Maternal maternal = residentMaternal(input.getMaternalId());
        if (input.getNurse() == null || input.getNurse().isBlank()) {
            throw new BizException("责任护士不能为空");
        }
        if (!Dict.isValidCareType(input.getCareType())) {
            throw new BizException("护理类型非法");
        }
        if (input.getStatus() != null && !input.getStatus().isBlank()
                && !Dict.CARE_PENDING.equals(input.getStatus())) {
            throw new BizException("新护理任务状态只能为待执行");
        }

        CareTask task = new CareTask();
        task.setMaternalId(maternal.getId());
        task.setNurse(input.getNurse().trim());
        task.setCareType(input.getCareType());
        task.setPlanDate(input.getPlanDate() == null ? LocalDate.now() : input.getPlanDate());
        task.setStatus(Dict.CARE_PENDING);
        task.setFinishTime(null);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return careRepository.save(task);
    }

    public CareTask update(Long id, CareTask input) {
        CareTask task = get(id);
        if (!Dict.CARE_PENDING.equals(task.getStatus())) {
            throw new BizException("已完成或已跳过的护理任务不可再修改");
        }
        if (input.getMaternalId() != null && !input.getMaternalId().equals(task.getMaternalId())) {
            Maternal maternal = residentMaternal(input.getMaternalId());
            task.setMaternalId(maternal.getId());
        }
        if (input.getNurse() != null && !input.getNurse().isBlank()) {
            task.setNurse(input.getNurse().trim());
        }
        if (input.getCareType() != null) {
            if (!Dict.isValidCareType(input.getCareType())) {
                throw new BizException("护理类型非法");
            }
            task.setCareType(input.getCareType());
        }
        if (input.getPlanDate() != null) {
            task.setPlanDate(input.getPlanDate());
        }
        if (input.getStatus() != null && !input.getStatus().equals(task.getStatus())) {
            throw new BizException("请使用完成或跳过操作变更护理任务状态");
        }
        return careRepository.save(task);
    }

    public CareTask complete(Long id) {
        CareTask task = get(id);
        if (Dict.CARE_SKIPPED.equals(task.getStatus())) {
            throw new BizException("已跳过的护理任务不可再打卡完成");
        }
        if (Dict.CARE_COMPLETED.equals(task.getStatus())) {
            throw new BizException("护理任务已完成，不可重复打卡");
        }
        task.setStatus(Dict.CARE_COMPLETED);
        task.setFinishTime(LocalDateTime.now());
        return careRepository.save(task);
    }

    public CareTask skip(Long id) {
        CareTask task = get(id);
        if (Dict.CARE_COMPLETED.equals(task.getStatus())) {
            throw new BizException("已完成的护理任务不可跳过");
        }
        if (Dict.CARE_SKIPPED.equals(task.getStatus())) {
            throw new BizException("护理任务已跳过，不可重复操作");
        }
        task.setStatus(Dict.CARE_SKIPPED);
        task.setFinishTime(null);
        return careRepository.save(task);
    }

    public Map<String, Object> completionRate(LocalDate planDate) {
        LocalDate targetDate = planDate == null ? LocalDate.now() : planDate;
        long total = careRepository.countByPlanDate(targetDate);
        long completed = careRepository.countByPlanDateAndStatus(targetDate, Dict.CARE_COMPLETED);
        int rate = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("planDate", targetDate);
        result.put("completedCount", completed);
        result.put("totalCount", total);
        result.put("completionRate", rate);
        return result;
    }

    private CareTask get(Long id) {
        return careRepository.findById(id).orElseThrow(() -> new BizException("护理任务不存在"));
    }

    private Maternal residentMaternal(Long maternalId) {
        if (maternalId == null) {
            throw new BizException("必须选择产妇");
        }
        Maternal maternal = maternalRepository.findById(maternalId)
                .orElseThrow(() -> new BizException("产妇档案不存在"));
        if (!Dict.MATERNAL_RESIDENT.equals(maternal.getStatus())) {
            throw new BizException("只能给在住产妇安排护理任务");
        }
        return maternal;
    }
}
