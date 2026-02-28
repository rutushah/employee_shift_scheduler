package com.schedular.web;

import com.schedular.domain.Day;
import com.schedular.domain.Employee;
import com.schedular.domain.Shift;
import com.schedular.domain.ShiftPreference;
import com.schedular.repo.EmployeeRepository;
import com.schedular.repo.ShiftRepository;
import com.schedular.service.SchedulingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class SchedulerController {

    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftPreferenceRepository;
    private final SchedulingService schedulingService;

    public SchedulerController(EmployeeRepository employeeRepository,
                               ShiftRepository shiftPreferenceRepository,
                               SchedulingService schedulingService) {
        this.employeeRepository = employeeRepository;
        this.shiftPreferenceRepository = shiftPreferenceRepository;
        this.schedulingService = schedulingService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/employees";
    }

    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        return "employees";
    }

    @PostMapping("/employees")
    public String addEmployee(@RequestParam String name) {
        name = name.trim();
        if (!name.isEmpty() && employeeRepository.findByNameIgnoreCase(name).isEmpty()) {
            employeeRepository.save(new Employee(name));
        }
        return "redirect:/employees";
    }

    @GetMapping("/preferences")
    public String preferences(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("days", Day.values());
        model.addAttribute("shifts", Shift.values());
        return "preferences";
    }

    /**
     * Saves ranked preferences for one employee.
     * Form fields expected: pref1_MON, pref2_MON, pref3_MON ... for all days
     */
    @PostMapping("/preferences/{employeeId}")
    public String savePreferences(@PathVariable Long employeeId,
                                  @RequestParam Map<String, String> params) {

        Employee emp = employeeRepository.findById(employeeId).orElseThrow();

        // Remove old prefs
        shiftPreferenceRepository.deleteByEmployee(emp);

        // Save new prefs
        for (Day day : Day.values()) {
            Shift s1 = Shift.valueOf(params.get("pref1_" + day.name()));
            Shift s2 = Shift.valueOf(params.get("pref2_" + day.name()));
            Shift s3 = Shift.valueOf(params.get("pref3_" + day.name()));

            shiftPreferenceRepository.save(new ShiftPreference(emp, day, 1, s1));
            shiftPreferenceRepository.save(new ShiftPreference(emp, day, 2, s2));
            shiftPreferenceRepository.save(new ShiftPreference(emp, day, 3, s3));
        }

        return "redirect:/preferences";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        model.addAttribute("days", Day.values());
        model.addAttribute("shifts", Shift.values()); // ✅ important for the table loop
        model.addAttribute("schedule", schedulingService.getScheduleView()); // ✅ safe view map
        return "schedule";
    }

    @PostMapping("/schedule/generate")
    public String generateSchedule() {
        schedulingService.generateSchedule();
        return "redirect:/schedule";
    }
}