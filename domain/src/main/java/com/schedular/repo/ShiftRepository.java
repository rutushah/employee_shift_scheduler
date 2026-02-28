package com.schedular.repo;

import com.schedular.domain.Day;
import com.schedular.domain.Employee;
import com.schedular.domain.ShiftPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<ShiftPreference, Long> {
    List<ShiftPreference> findByEmployeeIdOrderByDayAscRankAsc(Long employeeId);
    List<ShiftPreference> findByEmployee(Employee employee);
    void deleteByEmployee(Employee employee);
    List<ShiftPreference> findByDayOrderByRankAsc(Day day);
}