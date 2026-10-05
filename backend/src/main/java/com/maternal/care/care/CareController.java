package com.maternal.care.care;

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
import java.util.Map;

@RestController
@RequestMapping("/api/care-tasks")
public class CareController {

    private final CareService careService;

    public CareController(CareService careService) {
        this.careService = careService;
    }

    @GetMapping
    public ApiResponse<List<CareTask>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return ApiResponse.success(careService.list(planDate));
    }

    @GetMapping("/completion-rate")
    public ApiResponse<Map<String, Object>> completionRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return ApiResponse.success(careService.completionRate(planDate));
    }

    @PostMapping
    public ApiResponse<CareTask> create(@RequestBody CareTask input) {
        return ApiResponse.success(careService.create(input));
    }

    @PutMapping("/{id}")
    public ApiResponse<CareTask> update(@PathVariable Long id, @RequestBody CareTask input) {
        return ApiResponse.success(careService.update(id, input));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<CareTask> complete(@PathVariable Long id) {
        return ApiResponse.success(careService.complete(id));
    }

    @PostMapping("/{id}/skip")
    public ApiResponse<CareTask> skip(@PathVariable Long id) {
        return ApiResponse.success(careService.skip(id));
    }
}
