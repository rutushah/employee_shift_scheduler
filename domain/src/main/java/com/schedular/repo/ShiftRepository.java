package com.schedular.repo;

import com.schedular.domain.Day;
import com.schedular.domain.Employee;
import com.schedular.domain.ShiftPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ShiftRepository extends JpaRepository<ShiftPreference, Long> {
    List<ShiftPreference> findByEmployeeIdOrderByDayAscRankAsc(Long employeeId);
    List<ShiftPreference> findByEmployee(Employee employee);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShiftPreference p WHERE p.employee = :employee")
    void deleteByEmployee(@Param("employee") Employee employee);

    List<ShiftPreference> findByDayOrderByRankAsc(Day day);
}