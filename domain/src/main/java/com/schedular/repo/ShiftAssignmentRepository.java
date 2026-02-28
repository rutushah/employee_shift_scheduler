package com.schedular.repo;

import com.schedular.domain.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
}