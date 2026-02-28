package com.schedular.service;

import com.schedular.domain.*;
import com.schedular.repo.EmployeeRepository;
import com.schedular.repo.ShiftAssignmentRepository;
import com.schedular.repo.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SchedulingService {

    // Same logic as your console solution
    private static final int SHIFT_CAPACITY = 2;
    private static final int MIN_EMPLOYEES_PER_SHIFT = 2;
    private static final int MAX_DAYS_PER_EMPLOYEE = 5;

    private final EmployeeRepository employeeRepo;
    private final ShiftRepository shiftRepo;
    private final ShiftAssignmentRepository assignmentRepo;

    public SchedulingService(EmployeeRepository employeeRepo,
                             ShiftRepository shiftRepo,
                             ShiftAssignmentRepository assignmentRepo) {
        this.employeeRepo = employeeRepo;
        this.shiftRepo = shiftRepo;
        this.assignmentRepo = assignmentRepo;
    }

    @Transactional
    public void generateSchedule() {
        assignmentRepo.deleteAll();

        List<Employee> employees = employeeRepo.findAll();

        // employeeId -> day -> ordered shifts (ranked preference order)
        Map<Long, EnumMap<Day, List<Shift>>> prefs = loadPreferences(employees);

        // day -> shift -> employees assigned
        EnumMap<Day, EnumMap<Shift, List<Employee>>> schedule = initEmptySchedule();

        // employeeId -> day -> assigned shift (prevents >1 shift/day + tracks max 5 days)
        Map<Long, EnumMap<Day, Shift>> assignedByEmp = new HashMap<>();
        for (Employee e : employees) {
            assignedByEmp.put(e.getId(), new EnumMap<>(Day.class));
        }

        // Pass 1: preference assignment with conflict resolution
        for (Employee e : employees) {
            for (Day day : Day.values()) {
                if (assignedByEmp.get(e.getId()).size() >= MAX_DAYS_PER_EMPLOYEE) break;
                attemptAssignWithConflictResolution(e, day, schedule, assignedByEmp, prefs);
            }
        }

        // Pass 2: ensure minimum staffing using random fill
        ensureMinimumStaffing(schedule, assignedByEmp, employees, new Random());

        // Persist final schedule
        persist(schedule);
    }

    /**
     * Builds ranked preference list for each day for each employee.
     * If user stored only rank=1, we append missing shifts to the end.
     */
    private Map<Long, EnumMap<Day, List<Shift>>> loadPreferences(List<Employee> employees) {
        Map<Long, EnumMap<Day, List<Shift>>> prefs = new HashMap<>();

        for (Employee e : employees) {
            EnumMap<Day, List<Shift>> map = new EnumMap<>(Day.class);
            for (Day d : Day.values()) map.put(d, new ArrayList<>());

            // Pull from DB
            List<ShiftPreference> rows = shiftRepo.findByEmployeeIdOrderByDayAscRankAsc(e.getId());
            for (ShiftPreference p : rows) {
                map.get(p.getDay()).add(p.getShift());
            }

            // Ensure each day has full ordering of shifts
            for (Day d : Day.values()) {
                List<Shift> list = map.get(d);
                for (Shift s : Shift.values()) {
                    if (!list.contains(s)) list.add(s);
                }
            }

            prefs.put(e.getId(), map);
        }
        return prefs;
    }

    private EnumMap<Day, EnumMap<Shift, List<Employee>>> initEmptySchedule() {
        EnumMap<Day, EnumMap<Shift, List<Employee>>> schedule = new EnumMap<>(Day.class);
        for (Day day : Day.values()) {
            EnumMap<Shift, List<Employee>> shiftMap = new EnumMap<>(Shift.class);
            for (Shift shift : Shift.values()) shiftMap.put(shift, new ArrayList<>());
            schedule.put(day, shiftMap);
        }
        return schedule;
    }

    private boolean isAvailable(Employee e, Day day, Map<Long, EnumMap<Day, Shift>> assignedByEmp) {
        EnumMap<Day, Shift> assigned = assignedByEmp.get(e.getId());
        return !assigned.containsKey(day) && assigned.size() < MAX_DAYS_PER_EMPLOYEE;
    }

    /**
     * Conflict resolution:
     * - Try preferred shifts same day (in preference order)
     * - If full, try next days (same preference order for that next day)
     */
    private void attemptAssignWithConflictResolution(
            Employee emp,
            Day day,
            EnumMap<Day, EnumMap<Shift, List<Employee>>> schedule,
            Map<Long, EnumMap<Day, Shift>> assignedByEmp,
            Map<Long, EnumMap<Day, List<Shift>>> prefs
    ) {
        if (!isAvailable(emp, day, assignedByEmp)) return;

        List<Shift> prefList = prefs.get(emp.getId()).get(day);

        // Same day attempt (preference order)
        for (Shift s : prefList) {
            if (tryAssign(emp, day, s, schedule, assignedByEmp)) return;
        }

        // Next day(s)
        for (int i = day.ordinal() + 1; i < Day.values().length; i++) {
            Day nextDay = Day.values()[i];
            if (!isAvailable(emp, nextDay, assignedByEmp)) continue;

            List<Shift> nextPrefs = prefs.get(emp.getId()).get(nextDay);
            for (Shift s : nextPrefs) {
                if (tryAssign(emp, nextDay, s, schedule, assignedByEmp)) return;
            }
        }
    }

    private boolean tryAssign(
            Employee emp,
            Day day,
            Shift shift,
            EnumMap<Day, EnumMap<Shift, List<Employee>>> schedule,
            Map<Long, EnumMap<Day, Shift>> assignedByEmp
    ) {
        if (!isAvailable(emp, day, assignedByEmp)) return false;

        List<Employee> slot = schedule.get(day).get(shift);
        if (slot.size() >= SHIFT_CAPACITY) return false; // "full"

        slot.add(emp);
        assignedByEmp.get(emp.getId()).put(day, shift);
        return true;
    }

    private void ensureMinimumStaffing(
            EnumMap<Day, EnumMap<Shift, List<Employee>>> schedule,
            Map<Long, EnumMap<Day, Shift>> assignedByEmp,
            List<Employee> employees,
            Random rng
    ) {
        for (Day day : Day.values()) {
            for (Shift shift : Shift.values()) {
                List<Employee> slot = schedule.get(day).get(shift);

                while (slot.size() < MIN_EMPLOYEES_PER_SHIFT && slot.size() < SHIFT_CAPACITY) {
                    List<Employee> candidates = new ArrayList<>();
                    for (Employee e : employees) {
                        if (isAvailable(e, day, assignedByEmp)) candidates.add(e);
                    }
                    if (candidates.isEmpty()) break;

                    Employee chosen = candidates.get(rng.nextInt(candidates.size()));
                    slot.add(chosen);
                    assignedByEmp.get(chosen.getId()).put(day, shift);
                }
            }
        }
    }

    private void persist(EnumMap<Day, EnumMap<Shift, List<Employee>>> schedule) {
        for (Day day : Day.values()) {
            for (Shift shift : Shift.values()) {
                for (Employee e : schedule.get(day).get(shift)) {
                    assignmentRepo.save(new ShiftAssignment(e, day, shift));
                }
            }
        }
    }

    /**
     * For GUI display: Day -> Shift -> List<EmployeeName>
     */
    public EnumMap<Day, EnumMap<Shift, List<String>>> getScheduleView() {
        EnumMap<Day, EnumMap<Shift, List<String>>> view = new EnumMap<>(Day.class);
        for (Day day : Day.values()) {
            EnumMap<Shift, List<String>> shiftMap = new EnumMap<>(Shift.class);
            for (Shift shift : Shift.values()) {
                shiftMap.put(shift, new ArrayList<>());
            }
            view.put(day, shiftMap);
        }

        List<ShiftAssignment> all = assignmentRepo.findAll();
        for (ShiftAssignment a : all) {
            view.get(a.getDay()).get(a.getShift()).add(a.getEmployee().getName());
        }

        return view;
    }
}