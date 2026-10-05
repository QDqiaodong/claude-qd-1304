package com.maternal.care.room;

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
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ApiResponse<List<Room>> list() {
        return ApiResponse.success(roomService.list());
    }

    @PostMapping
    public ApiResponse<Room> create(@RequestBody Room input) {
        return ApiResponse.success(roomService.create(input));
    }

    @PutMapping("/{id}")
    public ApiResponse<Room> update(@PathVariable Long id, @RequestBody Room input) {
        return ApiResponse.success(roomService.update(id, input));
    }
}
