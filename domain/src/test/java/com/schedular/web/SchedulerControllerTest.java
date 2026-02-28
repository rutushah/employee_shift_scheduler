package com.schedular.web;

import com.schedular.domain.Day;
import com.schedular.domain.Employee;
import com.schedular.domain.Shift;
import com.schedular.domain.ShiftPreference;
import com.schedular.repo.EmployeeRepository;
import com.schedular.repo.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class SchedulerControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private ShiftRepository shiftRepo;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() {
        shiftRepo.deleteAll();
        employeeRepo.deleteAll();
    }

    @Test
    void home_redirectsToEmployees() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/", String.class);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertTrue(response.getHeaders().getLocation().getPath().endsWith("/employees"));
    }

    @Test
    void getEmployees_returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/employees", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void postEmployee_addsNewEmployee() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("name", "Alice");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/employees", request, String.class);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());

        assertTrue(employeeRepo.findByNameIgnoreCase("Alice").isPresent());
    }

    @Test
    void postEmployee_ignoresEmptyName() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("name", "   ");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        restTemplate.postForEntity(baseUrl() + "/employees", request, String.class);
        assertEquals(0, employeeRepo.count());
    }

    @Test
    void postEmployee_ignoresDuplicateName() {
        employeeRepo.save(new Employee("Alice"));
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("name", "alice");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        restTemplate.postForEntity(baseUrl() + "/employees", request, String.class);
        assertEquals(1, employeeRepo.count());
    }

    @Test
    void getPreferences_returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/preferences", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void postPreferences_savesPreferences() {
        Employee alice = employeeRepo.save(new Employee("Alice"));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        for (Day day : Day.values()) {
            form.add("pref1_" + day.name(), "MORNING");
            form.add("pref2_" + day.name(), "AFTERNOON");
            form.add("pref3_" + day.name(), "EVENING");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/preferences/" + alice.getId(), request, String.class);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());

        assertEquals(21, shiftRepo.findByEmployeeIdOrderByDayAscRankAsc(alice.getId()).size());
    }

    @Test
    void getSchedule_returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/schedule", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void postGenerateSchedule_generatesAndRedirects() {
        Employee alice = employeeRepo.save(new Employee("Alice"));
        Employee bob = employeeRepo.save(new Employee("Bob"));

        for (Day day : Day.values()) {
            shiftRepo.save(new ShiftPreference(alice, day, 1, Shift.MORNING));
            shiftRepo.save(new ShiftPreference(bob, day, 1, Shift.MORNING));
        }

        HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/schedule/generate", HttpMethod.POST, request, String.class);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
    }
}
