package com.maternal.care.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByOrderByRoomNoAsc();

    boolean existsByRoomNo(String roomNo);

    boolean existsByRoomNoAndIdNot(String roomNo, Long id);
}
