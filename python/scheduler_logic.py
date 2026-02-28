"""
Employee Shift Scheduler - Core Logic
Implements scheduling with conflict resolution and preference ranking.
"""

import random
from dataclasses import dataclass, field
from typing import Dict, List, Optional

DAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
SHIFTS = ["Morning", "Afternoon", "Evening"]

MIN_EMPLOYEES_PER_SHIFT = 2
MAX_DAYS_PER_EMPLOYEE = 5


@dataclass
class EmployeePreferences:
    """Employee with their shift preferences per day (ordered by priority: 1st, 2nd, 3rd)."""
    name: str
    # day -> [shift1, shift2, shift3] in order of preference (1st choice, 2nd, 3rd)
    preferences: Dict[str, List[str]] = field(default_factory=dict)

    def get_preferred_shifts(self, day: str) -> List[str]:
        """Return shifts for this day in preference order. Fills missing with remaining shifts."""
        prefs = self.preferences.get(day, [])
        remaining = [s for s in SHIFTS if s not in prefs]
        return prefs + remaining


def init_empty_schedule() -> Dict[str, Dict[str, List[str]]]:
    """Initialize empty schedule: day -> shift -> list of employee names."""
    return {day: {shift: [] for shift in SHIFTS} for day in DAYS}


def is_available(
    employee: str,
    day: str,
    assigned_by_emp: Dict[str, Dict[str, str]],
) -> bool:
    """Check if employee can work on this day (not assigned yet, under 5 days)."""
    assigned = assigned_by_emp.get(employee, {})
    return day not in assigned and len(assigned) < MAX_DAYS_PER_EMPLOYEE


def try_assign(
    employee: str,
    day: str,
    shift: str,
    schedule: Dict[str, Dict[str, List[str]]],
    assigned_by_emp: Dict[str, Dict[str, str]],
) -> bool:
    """Try to assign employee to shift. Returns True if successful."""
    if not is_available(employee, day, assigned_by_emp):
        return False

    slot = schedule[day][shift]
    if len(slot) >= MIN_EMPLOYEES_PER_SHIFT:
        return False  # Shift is full

    slot.append(employee)
    if employee not in assigned_by_emp:
        assigned_by_emp[employee] = {}
    assigned_by_emp[employee][day] = shift
    return True


def attempt_assign_with_conflict_resolution(
    emp_prefs: EmployeePreferences,
    day: str,
    schedule: Dict[str, Dict[str, List[str]]],
    assigned_by_emp: Dict[str, Dict[str, str]],
) -> bool:
    """
    Try to assign employee to their preferred shift. If full (conflict),
    try other shifts on same day, then next days. Returns True if assigned.
    """
    if not is_available(emp_prefs.name, day, assigned_by_emp):
        return False

    pref_list = emp_prefs.get_preferred_shifts(day)

    # Try preferred shifts on same day
    for shift in pref_list:
        if try_assign(emp_prefs.name, day, shift, schedule, assigned_by_emp):
            return True

    # Conflict: preferred shift full. Try same day with other shifts (already in pref_list)
    # pref_list has all 3, so we tried all. Try next days.
    day_idx = DAYS.index(day)
    for i in range(day_idx + 1, len(DAYS)):
        next_day = DAYS[i]
        if not is_available(emp_prefs.name, next_day, assigned_by_emp):
            continue
        next_prefs = emp_prefs.get_preferred_shifts(next_day)
        for shift in next_prefs:
            if try_assign(emp_prefs.name, next_day, shift, schedule, assigned_by_emp):
                return True

    return False


def ensure_minimum_staffing(
    schedule: Dict[str, Dict[str, List[str]]],
    assigned_by_emp: Dict[str, Dict[str, str]],
    employees: List[EmployeePreferences],
    rng: random.Random,
) -> None:
    """Ensure at least 2 employees per shift per day. Randomly assign if needed."""
    for day in DAYS:
        for shift in SHIFTS:
            slot = schedule[day][shift]

            while len(slot) < MIN_EMPLOYEES_PER_SHIFT:
                candidates = [
                    e.name for e in employees
                    if is_available(e.name, day, assigned_by_emp)
                ]
                if not candidates:
                    break

                chosen = rng.choice(candidates)
                try_assign(chosen, day, shift, schedule, assigned_by_emp)


def generate_schedule(
    employees: List[EmployeePreferences],
    seed: Optional[int] = None,
) -> Dict[str, Dict[str, List[str]]]:
    """
    Generate the weekly schedule from employee preferences.
    Returns schedule: day -> shift -> list of employee names.
    """
    rng = random.Random(seed)
    schedule = init_empty_schedule()
    assigned_by_emp: Dict[str, Dict[str, str]] = {}

    # First pass: assign by preferences
    for emp in employees:
        for day in DAYS:
            if len(assigned_by_emp.get(emp.name, {})) >= MAX_DAYS_PER_EMPLOYEE:
                break
            attempt_assign_with_conflict_resolution(emp, day, schedule, assigned_by_emp)

    # Second pass: ensure minimum staffing
    ensure_minimum_staffing(schedule, assigned_by_emp, employees, rng)

    return schedule


def format_schedule(schedule: Dict[str, Dict[str, List[str]]]) -> str:
    """Format schedule as readable text output."""
    lines = []
    lines.append("=" * 70)
    lines.append("WEEKLY EMPLOYEE SCHEDULE")
    lines.append("=" * 70)

    shift_width = 25
    for day in DAYS:
        lines.append(f"\n{day}")
        lines.append("-" * 40)
        for shift in SHIFTS:
            names = schedule[day][shift]
            names_str = ", ".join(names) if names else "(none)"
            lines.append(f"  {shift:12} : {names_str}")
    lines.append("\n" + "=" * 70)
    return "\n".join(lines)
