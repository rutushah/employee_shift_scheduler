"""
Employee Shift Scheduler - CLI Demo
Run without GUI for quick testing or scripting.
"""

from scheduler_logic import (
    DAYS,
    SHIFTS,
    EmployeePreferences,
    generate_schedule,
    format_schedule,
)


def main():
    # Example: 4 employees with preferences
    employees = [
        EmployeePreferences(
            name="Alice",
            preferences={
                "Mon": ["Morning", "Afternoon", "Evening"],
                "Tue": ["Morning", "Evening", "Afternoon"],
                "Wed": ["Afternoon", "Morning", "Evening"],
                "Thu": ["Evening", "Morning", "Afternoon"],
                "Fri": ["Morning", "Afternoon", "Evening"],
                "Sat": ["Afternoon", "Evening", "Morning"],
                "Sun": ["Evening", "Afternoon", "Morning"],
            },
        ),
        EmployeePreferences(
            name="Bob",
            preferences={
                "Mon": ["Afternoon", "Morning", "Evening"],
                "Tue": ["Evening", "Afternoon", "Morning"],
                "Wed": ["Morning", "Evening", "Afternoon"],
                "Thu": ["Morning", "Afternoon", "Evening"],
                "Fri": ["Afternoon", "Evening", "Morning"],
                "Sat": ["Morning", "Afternoon", "Evening"],
                "Sun": ["Afternoon", "Morning", "Evening"],
            },
        ),
        EmployeePreferences(
            name="Carol",
            preferences={
                day: ["Morning", "Afternoon", "Evening"] for day in DAYS
            },
        ),
        EmployeePreferences(
            name="Dave",
            preferences={
                day: ["Evening", "Afternoon", "Morning"] for day in DAYS
            },
        ),
        EmployeePreferences(
            name="Eve",
            preferences={
                day: ["Afternoon", "Morning", "Evening"] for day in DAYS
            },
        ),
        EmployeePreferences(
            name="Frank",
            preferences={
                day: ["Morning", "Evening", "Afternoon"] for day in DAYS
            },
        ),
    ]

    schedule = generate_schedule(employees)
    print(format_schedule(schedule))


if __name__ == "__main__":
    main()
