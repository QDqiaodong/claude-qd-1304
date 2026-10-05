package com.maternal.care.maternal;

import com.maternal.care.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maternals")
public class MaternalController {

    private final MaternalService maternalService;

    public MaternalController(MaternalService maternalService) {
        this.maternalService = maternalService;
    }

    @GetMapping
    public ApiResponse<List<Maternal>> list() {
        return ApiResponse.success(maternalService.list());
    }

    @PostMapping
    public ApiResponse<Maternal> create(@RequestBody Maternal input) {
        return ApiResponse.success(maternalService.create(input));
    }

    @PutMapping("/{id}")
    public ApiResponse<Maternal> update(@PathVariable Long id, @RequestBody Maternal input) {
        return ApiResponse.success(maternalService.update(id, input));
    }

    @PostMapping("/{id}/check-in")
    public ApiResponse<Maternal> checkIn(@PathVariable Long id) {
        return ApiResponse.success(maternalService.checkIn(id));
    }

    @PostMapping("/{id}/discharge")
    public ApiResponse<Maternal> discharge(@PathVariable Long id) {
        return ApiResponse.success(maternalService.discharge(id));
    }

    @PostMapping("/{id}/withdraw")
    public ApiResponse<Maternal> withdraw(@PathVariable Long id) {
        return ApiResponse.success(maternalService.withdraw(id));
    }
}
