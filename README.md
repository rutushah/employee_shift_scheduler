# Employee Shift Scheduler: A Comparative Implementation Report
## Abstract

This report presents the design and implementation of an employee shift scheduling application developed in two programming languages: Java and Python. The application manages weekly schedules for companies operating seven days per week, with employees selecting preferred shifts (morning, afternoon, or evening) for each day. Both implementations incorporate input and storage of employee preferences, scheduling logic that enforces business rules (maximum one shift per day, five days per week per employee, minimum two employees per shift), conflict detection and resolution when preferred shifts are full, and output of the final schedule in a readable format. The Java implementation uses Spring Boot with a web-based interface and persistent storage; the Python implementation uses a desktop GUI with tkinter. Both support priority-ranked shift preferences and demonstrate equivalent scheduling algorithms across different language paradigms.

## Introduction

Workforce scheduling is a common operational challenge for organizations that operate across multiple days and shifts. Manual scheduling is time-consuming and prone to errors, particularly when balancing employee preferences with organizational constraints (Ernst et al., 2004). This report describes the development of an employee shift scheduler application that automates the assignment of employees to shifts while respecting both individual preferences and company requirements.

The application was implemented in two languages—Java and Python—to fulfill requirements for demonstrating control structures and application logic across multiple programming paradigms. The core requirements include: (a) collecting and storing employee names and shift preferences for each day of the week; (b) assigning shifts under constraints that no employee works more than one shift per day or more than five days per week; (c) ensuring at least two employees per shift per day, with random assignment of additional employees when necessary; (d) detecting and resolving conflicts when an employee’s preferred shift is full by assigning them to another available shift on the same or next day; and (e) outputting the final schedule in a readable format. An optional enhancement—priority ranking of shift preferences—was implemented in both versions.

The remainder of this report provides a system overview of both implementations, describes the scheduling logic and data structures, and compares the architectural and user-interface approaches of the Java and Python solutions.

## System Overview

### Java Implementation

The Java implementation is built with Spring Boot 4.0 and follows a layered architecture typical of enterprise web applications. The system comprises the following components:

**Technology Stack.** The application uses Spring Boot Starter Web (MVC), Spring Data JPA for persistence, Thymeleaf for server-side HTML templating, and H2 as an in-memory database. The project is structured under the `com.schedular` package with domain, repository, service, and web layers.

**Domain Model.** Core entities include `Employee` (name, unique identifier), `Shift` (enum: MORNING, AFTERNOON, EVENING), `Day` (enum: MON through SUN), `ShiftPreference` (employee, day, rank, shift), and `ShiftAssignment` (employee, day, shift). Preferences are stored with a rank (1, 2, or 3) per day, enabling priority ordering.

**User Interface.** The web interface consists of three main views: (a) an Employees page for adding employee names; (b) a Preferences page where each employee sets ranked shift preferences (Rank 1, 2, 3) for each day via dropdown selects; and (c) a Schedule page that displays the generated weekly schedule in a table and provides a “Generate Schedule” button. Navigation links connect all three pages.

**Data Flow.** Employee and preference data are persisted in H2 via JPA repositories. The `SchedulingService` loads preferences, runs the scheduling algorithm, and persists assignments. The controller exposes REST-style endpoints for each page and for generating the schedule.

### Python Implementation

The Python implementation is a standalone desktop application with a graphical user interface (GUI) built using the standard library’s tkinter module. No external dependencies are required beyond the Python interpreter.

**Technology Stack.** The application uses Python 3.8+ with the standard library. The `scheduler_logic` module contains the core scheduling algorithm; the `employee_scheduler_gui` module provides the GUI. A separate `run_cli` script demonstrates the logic with sample data for testing without the GUI.

**Data Structures.** Employee preferences are represented by an `EmployeePreferences` dataclass with a name and a dictionary mapping each day to an ordered list of shifts (1st, 2nd, 3rd preference). The schedule is stored as a nested dictionary: day → shift → list of employee names. All data reside in memory; no database is used.

**User Interface.** The GUI uses a tabbed notebook. The Input tab allows users to add employees and, for each selected employee, set 1st, 2nd, and 3rd shift preferences per day via comboboxes. The Output tab displays the generated schedule in a scrollable text area and provides “Generate Schedule” and “Copy to Clipboard” buttons. The application switches to the Output tab automatically after generation.

## Scheduling Logic and Algorithm

Both implementations share the same scheduling algorithm, ensuring consistent behavior across languages.

### Constraints

1. **One shift per day.** No employee may be assigned more than one shift on a given day.
2. **Five days per week.** Each employee may work at most five days in the week.
3. **Minimum staffing.** Each shift on each day must have at least two employees. If fewer than two employees are available for a shift (e.g., due to preferences or prior assignments), the system randomly assigns additional employees who have not yet reached the five-day limit.

### Conflict Resolution

When an employee’s preferred shift for a day is full (already has two employees), a conflict occurs. The resolution strategy is:

1. Try the employee’s 1st, 2nd, and 3rd preferred shifts for that day.
2. If all are full, attempt assignment on the next day(s), again in preference order, until the employee is assigned or reaches five days.

This approach accommodates preferences as much as possible while guaranteeing that staffing requirements are met.

### Algorithm Steps

1. **Initialize.** Create an empty schedule (day × shift → list of employees) and a tracking structure for assignments per employee.
2. **First pass.** For each employee, iterate through days. For each day, attempt assignment to the employee’s preferred shifts in order. If a conflict occurs, try other shifts on the same day, then proceed to subsequent days.
3. **Second pass.** For each day and shift, if the slot has fewer than two employees, randomly select from available employees (not assigned that day, under five days) and assign until the minimum is reached or no candidates remain.
4. **Output.** Format the schedule for display (table in Java, formatted text in Python).

### Implementation Comparison

| Aspect | Java | Python |
|--------|------|--------|
| Data persistence | JPA/H2 database | In-memory only |
| Preference storage | `ShiftPreference` entities with rank | `EmployeePreferences` dataclass with dict |
| Schedule structure | `EnumMap<Day, EnumMap<Shift, List<Employee>>>` | `Dict[str, Dict[str, List[str]]]` |
| Conflict resolution | `attemptAssignWithConflictResolution` | `attempt_assign_with_conflict_resolution` |
| Minimum staffing | `ensureMinimumStaffing` with `Random` | `ensure_minimum_staffing` with `random.Random` |

## Control Structures and Language Features

### Java

The Java implementation employs standard control structures: `for` and `for-each` loops for iterating over days, shifts, and employees; `if` and `while` for conditionals and staffing loops; and `break` and `continue` for flow control. The use of enums (`Day`, `Shift`) and `EnumMap` provides type safety. Dependency injection and the repository pattern support testability and separation of concerns.

### Python

The Python implementation uses `for` loops over lists and dictionaries, `if`/`elif`/`else` for conditionals, and `while` for the minimum-staffing loop. The `dataclass` decorator simplifies the `EmployeePreferences` structure. Type hints (`Dict`, `List`, `Optional`) improve readability. The algorithm is implemented in pure functions, facilitating unit testing and reuse in both GUI and CLI contexts.


## Conclusion

Both the Java and Python implementations of the employee shift scheduler satisfy the specified requirements: input and storage of employee preferences, scheduling logic with business-rule enforcement, conflict detection and resolution, and readable output. The optional priority-ranking feature is supported in both. The Java version offers a web-based, persistent solution suitable for multi-user or server deployment; the Python version offers a lightweight, dependency-free desktop GUI suitable for single-user or scripting scenarios. The shared algorithm design ensures that scheduling outcomes are equivalent across implementations, while the differing architectures illustrate how the same problem can be addressed effectively in object-oriented (Java) and multi-paradigm (Python) languages.

## References

Ernst, A. T., Jiang, H., Krishnamoorthy, M., & Sier, D. (2004). Staff scheduling and rostering: A review of applications, methods and models. *European Journal of Operational Research*, *153*(1), 3–27. https://doi.org/10.1016/S0377-2217(03)00095-X
