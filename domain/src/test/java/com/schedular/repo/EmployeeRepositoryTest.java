package com.schedular.repo;

import com.schedular.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepo;

    @BeforeEach
    void setUp() {
        employeeRepo.deleteAll();
    }

    @Test
    void save_createsEmployee() {
        Employee emp = employeeRepo.save(new Employee("Alice"));

        assertNotNull(emp.getId());
        assertEquals("Alice", emp.getName());
    }

    @Test
    void findByNameIgnoreCase_returnsEmployee_whenExists() {
        employeeRepo.save(new Employee("Alice"));

        Optional<Employee> found = employeeRepo.findByNameIgnoreCase("alice");

        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }

    @Test
    void findByNameIgnoreCase_returnsEmpty_whenNotExists() {
        Optional<Employee> found = employeeRepo.findByNameIgnoreCase("Unknown");

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_returnsAllEmployees() {
        employeeRepo.save(new Employee("Alice"));
        employeeRepo.save(new Employee("Bob"));

        assertEquals(2, employeeRepo.findAll().size());
    }
}
