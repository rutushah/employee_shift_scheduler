# Employee Shift Scheduler (Python)

A Python application for managing employee schedules with shift preferences and conflict resolution.

## Requirements Met

1. **Input and Storage**: Collects employee names and preferred shifts (morning, afternoon, evening) for each day of the week. Stored in appropriate data structures.

2. **Scheduling Logic**:
   - No employee works more than one shift per day
   - Maximum 5 days per week per employee
   - At least 2 employees per shift per day; if fewer available, randomly assigns additional employees

3. **Shift Conflicts**: Detects when preferred shift is full and assigns to another available shift on the same or next day.

4. **Output**: Displays the final weekly schedule in a readable format.

5. **Bonus - Priority Ranking**: Employees can set 1st, 2nd, and 3rd preference per day. The scheduler accommodates preferences as much as possible while meeting requirements.

6. **GUI**: Full GUI for input (employee names + preferences) and output (schedule display).

## How to Run

### GUI (recommended)

```bash
cd python
python employee_scheduler_gui.py
```

### CLI (demo / testing)

```bash
cd python
python run_cli.py
```

## Usage

1. **Add Employees**: Enter a name and click "Add Employee".
2. **Set Preferences**: Select an employee, then for each day set their 1st, 2nd, and 3rd shift preference (Morning, Afternoon, Evening).
3. **Generate Schedule**: Click "Generate Schedule" to run the scheduler. The output tab shows the weekly schedule.
4. **Copy**: Use "Copy to Clipboard" to copy the schedule text.

## Files

- `scheduler_logic.py` - Core scheduling logic (no GUI)
- `employee_scheduler_gui.py` - GUI application
- `run_cli.py` - CLI demo with sample data
- `requirements.txt` - Dependencies (none required; uses stdlib)

## Requirements

- Python 3.8+
- tkinter (included with standard Python; on Linux you may need `python3-tk`)
