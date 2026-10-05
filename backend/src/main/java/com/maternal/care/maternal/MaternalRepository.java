package com.maternal.care.maternal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaternalRepository extends JpaRepository<Maternal, Long> {
    List<Maternal> findAllByOrderByIdDesc();
}
