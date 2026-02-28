package com.schedular.repo;

import com.schedular.domain.Day;
import com.schedular.domain.Employee;
import com.schedular.domain.Shift;
import com.schedular.domain.ShiftPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ShiftRepositoryTest {

    @Autowired
    private ShiftRepository shiftRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    private Employee alice;

    @BeforeEach
    void setUp() {
        shiftRepo.deleteAll();
        employeeRepo.deleteAll();
        alice = employeeRepo.save(new Employee("Alice"));
    }

    @Test
    void save_createsShiftPreference() {
        ShiftPreference pref = shiftRepo.save(new ShiftPreference(alice, Day.MON, 1, Shift.MORNING));

        assertNotNull(pref.getId());
        assertEquals(alice.getId(), pref.getEmployee().getId());
        assertEquals(Day.MON, pref.getDay());
        assertEquals(1, pref.getRank());
        assertEquals(Shift.MORNING, pref.getShift());
    }

    @Test
    void findByEmployeeIdOrderByDayAscRankAsc_returnsInCorrectOrder() {
        shiftRepo.save(new ShiftPreference(alice, Day.WED, 1, Shift.MORNING));
        shiftRepo.save(new ShiftPreference(alice, Day.MON, 1, Shift.MORNING));
        shiftRepo.save(new ShiftPreference(alice, Day.MON, 2, Shift.AFTERNOON));

        List<ShiftPreference> prefs = shiftRepo.findByEmployeeIdOrderByDayAscRankAsc(alice.getId());

        assertEquals(3, prefs.size());
        assertEquals(Day.MON, prefs.get(0).getDay());
        assertEquals(1, prefs.get(0).getRank());
        assertEquals(Day.MON, prefs.get(1).getDay());
        assertEquals(2, prefs.get(1).getRank());
        assertEquals(Day.WED, prefs.get(2).getDay());
    }

    @Test
    void deleteByEmployee_removesAllPreferencesForEmployee() {
        shiftRepo.save(new ShiftPreference(alice, Day.MON, 1, Shift.MORNING));
        shiftRepo.save(new ShiftPreference(alice, Day.TUE, 1, Shift.MORNING));

        shiftRepo.deleteByEmployee(alice);

        assertEquals(0, shiftRepo.findByEmployeeIdOrderByDayAscRankAsc(alice.getId()).size());
    }
}
