package com.maternal.care.maternal;

import com.maternal.care.common.BizException;
import com.maternal.care.common.Dict;
import com.maternal.care.room.Room;
import com.maternal.care.room.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaternalService {

    private final MaternalRepository maternalRepository;
    private final RoomRepository roomRepository;

    public MaternalService(MaternalRepository maternalRepository, RoomRepository roomRepository) {
        this.maternalRepository = maternalRepository;
        this.roomRepository = roomRepository;
    }

    public List<Maternal> list() {
        return maternalRepository.findAllByOrderByIdDesc();
    }

    public Maternal create(Maternal input) {
        validateRequired(input);
        roomRepository.findById(input.getRoomId())
                .orElseThrow(() -> new BizException("关联房间不存在"));

        LocalDate checkInDate = input.getCheckInDate() == null ? LocalDate.now() : input.getCheckInDate();
        validateBirthDate(checkInDate, input.getBabyBirthDate());
        if (input.getStatus() != null && !input.getStatus().isBlank()
                && !Dict.MATERNAL_PENDING.equals(input.getStatus())) {
            throw new BizException("新登记产妇状态只能为待入住，请通过入住操作办理入住");
        }

        Maternal maternal = new Maternal();
        maternal.setName(input.getName().trim());
        maternal.setPhone(input.getPhone().trim());
        maternal.setRoomId(input.getRoomId());
        maternal.setCheckInDate(checkInDate);
        maternal.setDueDate(input.getDueDate());
        maternal.setBabyBirthDate(input.getBabyBirthDate());
        maternal.setStatus(Dict.MATERNAL_PENDING);
        maternal.setCreatedAt(LocalDateTime.now());
        maternal.setUpdatedAt(LocalDateTime.now());
        return maternalRepository.save(maternal);
    }

    public Maternal update(Long id, Maternal input) {
        Maternal maternal = get(id);
        ensureNotTerminal(maternal);

        if (input.getName() != null && !input.getName().isBlank()) {
            maternal.setName(input.getName().trim());
        }
        if (input.getPhone() != null && !input.getPhone().isBlank()) {
            maternal.setPhone(input.getPhone().trim());
        }
        if (input.getRoomId() != null && !input.getRoomId().equals(maternal.getRoomId())) {
            if (Dict.MATERNAL_RESIDENT.equals(maternal.getStatus())) {
                throw new BizException("在住产妇不可直接更换房间");
            }
            roomRepository.findById(input.getRoomId())
                    .orElseThrow(() -> new BizException("关联房间不存在"));
            maternal.setRoomId(input.getRoomId());
        }
        LocalDate checkInDate = input.getCheckInDate() == null
                ? maternal.getCheckInDate() : input.getCheckInDate();
        LocalDate babyBirthDate = input.getBabyBirthDate() == null
                ? maternal.getBabyBirthDate() : input.getBabyBirthDate();
        validateBirthDate(checkInDate, babyBirthDate);
        if (input.getCheckInDate() != null) {
            maternal.setCheckInDate(input.getCheckInDate());
        }
        if (input.getDueDate() != null) {
            maternal.setDueDate(input.getDueDate());
        }
        if (input.getBabyBirthDate() != null) {
            maternal.setBabyBirthDate(input.getBabyBirthDate());
        }
        if (input.getStatus() != null && !input.getStatus().equals(maternal.getStatus())) {
            throw new BizException("请使用入住、出院或退住操作变更产妇状态");
        }
        return maternalRepository.save(maternal);
    }

    @Transactional
    public Maternal checkIn(Long id) {
        Maternal maternal = get(id);
        ensureNotTerminal(maternal);
        if (!Dict.MATERNAL_PENDING.equals(maternal.getStatus())) {
            throw new BizException("只有待入住产妇可以办理入住");
        }
        Room room = roomRepository.findById(maternal.getRoomId())
                .orElseThrow(() -> new BizException("关联房间不存在"));
        if (!Dict.ROOM_AVAILABLE.equals(room.getStatus())) {
            throw new BizException("只有空闲房间可办理入住，当前房间状态为" + room.getStatus());
        }
        room.setStatus(Dict.ROOM_OCCUPIED);
        maternal.setStatus(Dict.MATERNAL_RESIDENT);
        roomRepository.save(room);
        return maternalRepository.save(maternal);
    }

    @Transactional
    public Maternal discharge(Long id) {
        Maternal maternal = get(id);
        ensureNotTerminal(maternal);
        if (!Dict.MATERNAL_RESIDENT.equals(maternal.getStatus())) {
            throw new BizException("只有在住产妇可以办理出院");
        }
        cleanRoom(maternal.getRoomId());
        maternal.setStatus(Dict.MATERNAL_DISCHARGED);
        return maternalRepository.save(maternal);
    }

    @Transactional
    public Maternal withdraw(Long id) {
        Maternal maternal = get(id);
        ensureNotTerminal(maternal);
        cleanRoom(maternal.getRoomId());
        maternal.setStatus(Dict.MATERNAL_WITHDRAWN);
        return maternalRepository.save(maternal);
    }

    public Maternal get(Long id) {
        return maternalRepository.findById(id).orElseThrow(() -> new BizException("产妇档案不存在"));
    }

    private void validateRequired(Maternal input) {
        if (input.getName() == null || input.getName().isBlank()) {
            throw new BizException("产妇姓名不能为空");
        }
        if (input.getPhone() == null || input.getPhone().isBlank()) {
            throw new BizException("联系电话不能为空");
        }
        if (input.getRoomId() == null) {
            throw new BizException("必须选择房间");
        }
        if (input.getDueDate() == null) {
            throw new BizException("预产期不能为空");
        }
    }

    private void validateBirthDate(LocalDate checkInDate, LocalDate babyBirthDate) {
        if (babyBirthDate != null && checkInDate != null && babyBirthDate.isBefore(checkInDate)) {
            throw new BizException("分娩日期不得早于入住日期");
        }
    }

    private void ensureNotTerminal(Maternal maternal) {
        if (Dict.MATERNAL_DISCHARGED.equals(maternal.getStatus())
                || Dict.MATERNAL_WITHDRAWN.equals(maternal.getStatus())) {
            throw new BizException("已出院或退住为终态，不可再修改");
        }
    }

    private void cleanRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BizException("关联房间不存在"));
        room.setStatus(Dict.ROOM_CLEANING);
        roomRepository.save(room);
    }
}
