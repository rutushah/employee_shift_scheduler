package com.schedular.service;

import com.schedular.domain.*;
import com.schedular.repo.EmployeeRepository;
import com.schedular.repo.ShiftAssignmentRepository;
import com.schedular.repo.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SchedulingServiceTest {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private ShiftRepository shiftRepo;

    @Autowired
    private ShiftAssignmentRepository assignmentRepo;

    @BeforeEach
    void setUp() {
        assignmentRepo.deleteAll();
        shiftRepo.deleteAll();
        employeeRepo.deleteAll();
    }

    @Test
    void getScheduleView_returnsEmptySchedule_whenNoAssignments() {
        List<SchedulingService.ScheduleRow> view = schedulingService.getScheduleView();

        assertEquals(7, view.size(), "Should have 7 days");
        for (SchedulingService.ScheduleRow row : view) {
            assertNotNull(row.day());
            assertNotNull(row.shiftCells());
            assertEquals(3, row.shiftCells().size(), "Each day should have 3 shifts");
            for (String cell : row.shiftCells().values()) {
                assertEquals("", cell, "Empty schedule should have empty cells");
            }
        }
    }

    @Test
    void getScheduleView_returnsCorrectData_afterScheduleGenerated() {
        Employee alice = employeeRepo.save(new Employee("Alice"));
        Employee bob = employeeRepo.save(new Employee("Bob"));

        for (Day day : Day.values()) {
            shiftRepo.save(new ShiftPreference(alice, day, 1, Shift.MORNING));
            shiftRepo.save(new ShiftPreference(alice, day, 2, Shift.AFTERNOON));
            shiftRepo.save(new ShiftPreference(alice, day, 3, Shift.EVENING));
            shiftRepo.save(new ShiftPreference(bob, day, 1, Shift.MORNING));
            shiftRepo.save(new ShiftPreference(bob, day, 2, Shift.AFTERNOON));
            shiftRepo.save(new ShiftPreference(bob, day, 3, Shift.EVENING));
        }

        schedulingService.generateSchedule();
        List<SchedulingService.ScheduleRow> view = schedulingService.getScheduleView();

        assertFalse(view.isEmpty());
        boolean hasNonEmptyCell = view.stream()
                .flatMap(row -> row.shiftCells().values().stream())
                .anyMatch(cell -> !cell.isEmpty());
        assertTrue(hasNonEmptyCell, "Schedule should have at least one assigned shift");
    }

    @Test
    void generateSchedule_noEmployeeWorksMoreThanOneShiftPerDay() {
        Employee alice = employeeRepo.save(new Employee("Alice"));
        Employee bob = employeeRepo.save(new Employee("Bob"));
        Employee charlie = employeeRepo.save(new Employee("Charlie"));
        Employee dave = employeeRepo.save(new Employee("Dave"));

        for (Day day : Day.values()) {
            for (Employee e : List.of(alice, bob, charlie, dave)) {
                shiftRepo.save(new ShiftPreference(e, day, 1, Shift.MORNING));
                shiftRepo.save(new ShiftPreference(e, day, 2, Shift.AFTERNOON));
                shiftRepo.save(new ShiftPreference(e, day, 3, Shift.EVENING));
            }
        }

        schedulingService.generateSchedule();

        Map<Long, Long> assignmentsPerEmployee = assignmentRepo.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getEmployee().getId(),
                        java.util.stream.Collectors.counting()
                ));

        for (Long empId : assignmentsPerEmployee.keySet()) {
            long count = assignmentsPerEmployee.get(empId);
            assertTrue(count <= 5, "Employee should work max 5 days, got " + count);
        }

        long assignmentsPerDay = assignmentRepo.findAll().stream()
                .map(a -> a.getEmployee().getId() + "-" + a.getDay())
                .distinct()
                .count();
        long totalAssignments = assignmentRepo.count();
        assertEquals(totalAssignments, assignmentsPerDay,
                "Each employee should have at most one shift per day");
    }

    @Test
    void generateSchedule_noEmployeeWorksMoreThan5Days() {
        Employee alice = employeeRepo.save(new Employee("Alice"));
        Employee bob = employeeRepo.save(new Employee("Bob"));

        for (Day day : Day.values()) {
            shiftRepo.save(new ShiftPreference(alice, day, 1, Shift.MORNING));
            shiftRepo.save(new ShiftPreference(alice, day, 2, Shift.AFTERNOON));
            shiftRepo.save(new ShiftPreference(alice, day, 3, Shift.EVENING));
            shiftRepo.save(new ShiftPreference(bob, day, 1, Shift.MORNING));
            shiftRepo.save(new ShiftPreference(bob, day, 2, Shift.AFTERNOON));
            shiftRepo.save(new ShiftPreference(bob, day, 3, Shift.EVENING));
        }

        schedulingService.generateSchedule();

        for (Employee e : List.of(alice, bob)) {
            long daysWorked = assignmentRepo.findAll().stream()
                    .filter(a -> a.getEmployee().getId().equals(e.getId()))
                    .map(a -> a.getDay())
                    .distinct()
                    .count();
            assertTrue(daysWorked <= 5, "Employee " + e.getName() + " should work max 5 days, got " + daysWorked);
        }
    }

    @Test
    void generateSchedule_atLeast2EmployeesPerShiftPerDay() {
        Employee alice = employeeRepo.save(new Employee("Alice"));
        Employee bob = employeeRepo.save(new Employee("Bob"));
        Employee charlie = employeeRepo.save(new Employee("Charlie"));
        Employee dave = employeeRepo.save(new Employee("Dave"));

        for (Day day : Day.values()) {
            for (Employee e : List.of(alice, bob, charlie, dave)) {
                shiftRepo.save(new ShiftPreference(e, day, 1, Shift.MORNING));
                shiftRepo.save(new ShiftPreference(e, day, 2, Shift.AFTERNOON));
                shiftRepo.save(new ShiftPreference(e, day, 3, Shift.EVENING));
            }
        }

        schedulingService.generateSchedule();

        for (Day day : Day.values()) {
            for (Shift shift : Shift.values()) {
                long count = assignmentRepo.findAll().stream()
                        .filter(a -> a.getDay() == day && a.getShift() == shift)
                        .count();
                assertTrue(count >= 2,
                        "Day " + day + " shift " + shift + " should have at least 2 employees, got " + count);
            }
        }
    }

    @Test
    void getPreferencesByEmployee_returnsEmpty_whenNoPreferences() {
        Employee alice = employeeRepo.save(new Employee("Alice"));

        Map<Long, Map<String, Map<Integer, String>>> prefs = schedulingService.getPreferencesByEmployee();

        assertTrue(prefs.containsKey(alice.getId()));
        assertTrue(prefs.get(alice.getId()).isEmpty());
    }

    @Test
    void getPreferencesByEmployee_returnsCorrectData_whenPreferencesExist() {
        Employee alice = employeeRepo.save(new Employee("Alice"));
        shiftRepo.save(new ShiftPreference(alice, Day.MON, 1, Shift.MORNING));
        shiftRepo.save(new ShiftPreference(alice, Day.MON, 2, Shift.AFTERNOON));
        shiftRepo.save(new ShiftPreference(alice, Day.MON, 3, Shift.EVENING));

        Map<Long, Map<String, Map<Integer, String>>> prefs = schedulingService.getPreferencesByEmployee();

        assertTrue(prefs.containsKey(alice.getId()));
        Map<String, Map<Integer, String>> alicePrefs = prefs.get(alice.getId());
        assertTrue(alicePrefs.containsKey("MON"));
        assertEquals("MORNING", alicePrefs.get("MON").get(1));
        assertEquals("AFTERNOON", alicePrefs.get("MON").get(2));
        assertEquals("EVENING", alicePrefs.get("MON").get(3));
    }

    @Test
    void generateSchedule_handlesEmptyEmployeeList() {
        schedulingService.generateSchedule();

        List<SchedulingService.ScheduleRow> view = schedulingService.getScheduleView();
        assertEquals(7, view.size());
        assertEquals(0, assignmentRepo.count());
    }
}
