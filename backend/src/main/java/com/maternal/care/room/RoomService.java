package com.maternal.care.room;

import com.maternal.care.common.BizException;
import com.maternal.care.common.Dict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> list() {
        return roomRepository.findAllByOrderByRoomNoAsc();
    }

    public Room create(Room input) {
        if (input.getRoomNo() == null || input.getRoomNo().isBlank()) {
            throw new BizException("房号不能为空");
        }
        if (roomRepository.existsByRoomNo(input.getRoomNo().trim())) {
            throw new BizException("房号已存在");
        }
        if (!Dict.isValidRoomType(input.getType())) {
            throw new BizException("房型必须是：标准间、单间或套房");
        }
        if (input.getFloor() == null || input.getFloor().isBlank()) {
            throw new BizException("楼层不能为空");
        }
        validateMonthlyFee(input.getMonthlyFee());

        String status = input.getStatus();
        if (status == null || status.isBlank()) {
            status = Dict.ROOM_AVAILABLE;
        }
        if (!Dict.isValidRoomStatus(status)) {
            throw new BizException("房间状态非法");
        }

        Room room = new Room();
        room.setRoomNo(input.getRoomNo().trim());
        room.setType(input.getType());
        room.setFloor(input.getFloor().trim());
        room.setMonthlyFee(input.getMonthlyFee());
        room.setStatus(status);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepository.save(room);
    }

    public Room update(Long id, Room input) {
        Room room = get(id);
        if (input.getRoomNo() != null && !input.getRoomNo().isBlank()) {
            String roomNo = input.getRoomNo().trim();
            if (roomRepository.existsByRoomNoAndIdNot(roomNo, id)) {
                throw new BizException("房号已存在");
            }
            room.setRoomNo(roomNo);
        }
        if (input.getType() != null) {
            if (!Dict.isValidRoomType(input.getType())) {
                throw new BizException("房型必须是：标准间、单间或套房");
            }
            room.setType(input.getType());
        }
        if (input.getFloor() != null && !input.getFloor().isBlank()) {
            room.setFloor(input.getFloor().trim());
        }
        if (input.getMonthlyFee() != null) {
            validateMonthlyFee(input.getMonthlyFee());
            room.setMonthlyFee(input.getMonthlyFee());
        }
        if (input.getStatus() != null && !input.getStatus().isBlank()
                && !input.getStatus().equals(room.getStatus())) {
            if (!Dict.isValidRoomStatus(input.getStatus())) {
                throw new BizException("房间状态非法");
            }
            if (Dict.ROOM_OCCUPIED.equals(room.getStatus())) {
                throw new BizException("在住房间不可直接变更状态，请先为产妇办理出院或退住");
            }
            room.setStatus(input.getStatus());
        }
        return roomRepository.save(room);
    }

    public Room get(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new BizException("房间不存在"));
    }

    private void validateMonthlyFee(BigDecimal monthlyFee) {
        if (monthlyFee == null || monthlyFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("月费必须大于0");
        }
    }
}
