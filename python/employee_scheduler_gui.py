"""
Employee Shift Scheduler - GUI Application
Input: Employee names and shift preferences (with priority ranking)
Output: Weekly schedule display
"""

import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext
from typing import Dict, List

from scheduler_logic import (
    DAYS,
    SHIFTS,
    EmployeePreferences,
    generate_schedule,
    format_schedule,
)


class EmployeeSchedulerApp:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Employee Shift Scheduler")
        self.root.geometry("900x700")
        self.root.minsize(800, 600)

        # Data: employee name -> preferences {day: [shift1, shift2, shift3]}
        self.employees: Dict[str, Dict[str, List[str]]] = {}

        self._setup_styles()
        self._build_ui()

    def _setup_styles(self):
        style = ttk.Style()
        style.configure("Header.TLabel", font=("Helvetica", 12, "bold"))
        style.configure("Schedule.TLabel", font=("Consolas", 10))

    def _build_ui(self):
        # Main notebook
        self.notebook = ttk.Notebook(self.root)
        self.notebook.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)

        # Tab 1: Input (Employees & Preferences)
        input_frame = ttk.Frame(self.notebook, padding=10)
        self.notebook.add(input_frame, text="Input - Employees & Preferences")

        # Employee section
        emp_section = ttk.LabelFrame(input_frame, text="Employees", padding=10)
        emp_section.pack(fill=tk.X, pady=(0, 10))

        emp_row = ttk.Frame(emp_section)
        emp_row.pack(fill=tk.X)
        ttk.Label(emp_row, text="Employee name:").pack(side=tk.LEFT, padx=(0, 5))
        self.emp_entry = ttk.Entry(emp_row, width=25)
        self.emp_entry.pack(side=tk.LEFT, padx=(0, 5))
        self.emp_entry.bind("<Return>", lambda e: self._add_employee())
        ttk.Button(emp_row, text="Add Employee", command=self._add_employee).pack(side=tk.LEFT)

        self.emp_listbox = tk.Listbox(emp_section, height=6, selectmode=tk.SINGLE)
        self.emp_listbox.pack(fill=tk.X, pady=(5, 0))
        self.emp_listbox.bind("<<ListboxSelect>>", self._on_employee_select)

        ttk.Button(emp_section, text="Remove Selected", command=self._remove_employee).pack(pady=(5, 0))

        # Preferences section
        pref_section = ttk.LabelFrame(input_frame, text="Shift Preferences (Priority: 1st, 2nd, 3rd per day)", padding=10)
        pref_section.pack(fill=tk.BOTH, expand=True, pady=(0, 10))

        ttk.Label(
            pref_section,
            text="Select an employee above, then set their preferred shift order for each day.",
            font=("Helvetica", 9),
        ).pack(anchor=tk.W)

        # Create scrollable frame for preferences
        pref_canvas = tk.Canvas(pref_section)
        pref_scrollbar = ttk.Scrollbar(pref_section, orient=tk.VERTICAL, command=pref_canvas.yview)

        self.pref_inner = ttk.Frame(pref_canvas)
        self.pref_inner.bind(
            "<Configure>",
            lambda e: pref_canvas.configure(scrollregion=pref_canvas.bbox("all")),
        )
        pref_canvas.create_window((0, 0), window=self.pref_inner, anchor=tk.NW)
        pref_canvas.configure(yscrollcommand=pref_scrollbar.set)

        pref_canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        pref_scrollbar.pack(side=tk.RIGHT, fill=tk.Y)

        # Header row
        header = ttk.Frame(self.pref_inner)
        header.pack(fill=tk.X, pady=(0, 5))
        ttk.Label(header, text="Day", width=8).pack(side=tk.LEFT, padx=2)
        ttk.Label(header, text="1st choice", width=12).pack(side=tk.LEFT, padx=2)
        ttk.Label(header, text="2nd choice", width=12).pack(side=tk.LEFT, padx=2)
        ttk.Label(header, text="3rd choice", width=12).pack(side=tk.LEFT, padx=2)

        self.pref_vars: Dict[str, Dict[int, tk.StringVar]] = {}
        for day in DAYS:
            row = ttk.Frame(self.pref_inner)
            row.pack(fill=tk.X, pady=2)
            ttk.Label(row, text=day, width=8).pack(side=tk.LEFT, padx=2)
            vars_day = {}
            for i in range(3):
                var = tk.StringVar(value=SHIFTS[i])
                combo = ttk.Combobox(row, textvariable=var, values=SHIFTS, width=10, state="readonly")
                combo.pack(side=tk.LEFT, padx=2)
                vars_day[i] = var
            self.pref_vars[day] = vars_day

        # Tab 2: Output (Schedule)
        output_frame = ttk.Frame(self.notebook, padding=10)
        self.notebook.add(output_frame, text="Output - Schedule")

        btn_frame = ttk.Frame(output_frame)
        btn_frame.pack(fill=tk.X, pady=(0, 10))
        ttk.Button(btn_frame, text="Generate Schedule", command=self._generate_and_show).pack(side=tk.LEFT, padx=(0, 5))
        ttk.Button(btn_frame, text="Copy to Clipboard", command=self._copy_schedule).pack(side=tk.LEFT)

        self.schedule_text = scrolledtext.ScrolledText(
            output_frame,
            wrap=tk.WORD,
            font=("Consolas", 10),
            height=25,
        )
        self.schedule_text.pack(fill=tk.BOTH, expand=True)
        self.schedule_text.insert(tk.END, "Click 'Generate Schedule' to create the weekly schedule.")
        self.schedule_text.config(state=tk.DISABLED)

        self._last_schedule: Dict[str, Dict[str, List[str]]] | None = None

    def _add_employee(self):
        name = self.emp_entry.get().strip()
        if not name:
            messagebox.showwarning("Warning", "Please enter an employee name.")
            return
        if name in self.employees:
            messagebox.showwarning("Warning", f"Employee '{name}' already exists.")
            return
        self.employees[name] = {day: list(SHIFTS) for day in DAYS}  # default order
        self.emp_listbox.insert(tk.END, name)
        self.emp_entry.delete(0, tk.END)

    def _remove_employee(self):
        sel = self.emp_listbox.curselection()
        if not sel:
            messagebox.showwarning("Warning", "Please select an employee to remove.")
            return
        idx = sel[0]
        name = self.emp_listbox.get(idx)
        del self.employees[name]
        self.emp_listbox.delete(idx)
        self._refresh_pref_display()

    def _on_employee_select(self, event):
        sel = self.emp_listbox.curselection()
        if sel:
            self._load_preferences_for(self.emp_listbox.get(sel[0]))

    def _load_preferences_for(self, name: str):
        prefs = self.employees.get(name)
        if not prefs:
            return
        for day in DAYS:
            shifts = prefs.get(day, list(SHIFTS))
            for i in range(3):
                self.pref_vars[day][i].set(shifts[i] if i < len(shifts) else "")

    def _save_current_preferences(self):
        sel = self.emp_listbox.curselection()
        if not sel:
            return
        name = self.emp_listbox.get(sel[0])
        if name not in self.employees:
            return
        for day in DAYS:
            shifts = []
            seen = set()
            for i in range(3):
                val = self.pref_vars[day][i].get()
                if val and val in SHIFTS and val not in seen:
                    shifts.append(val)
                    seen.add(val)
            for s in SHIFTS:
                if s not in seen:
                    shifts.append(s)
            self.employees[name][day] = shifts

    def _refresh_pref_display(self):
        sel = self.emp_listbox.curselection()
        if sel:
            self._load_preferences_for(self.emp_listbox.get(sel[0]))
        else:
            for day in DAYS:
                for i in range(3):
                    self.pref_vars[day][i].set(SHIFTS[i] if i < len(SHIFTS) else "")

    def _generate_and_show(self):
        self._save_current_preferences()

        if len(self.employees) < 2:
            messagebox.showerror("Error", "Add at least 2 employees to generate a schedule.")
            return

        emp_list = [
            EmployeePreferences(name=name, preferences=prefs)
            for name, prefs in self.employees.items()
        ]
        schedule = generate_schedule(emp_list)
        self._last_schedule = schedule

        formatted = format_schedule(schedule)
        self.schedule_text.config(state=tk.NORMAL)
        self.schedule_text.delete(1.0, tk.END)
        self.schedule_text.insert(tk.END, formatted)
        self.schedule_text.config(state=tk.DISABLED)

        # Switch to output tab
        self.notebook.select(1)

        messagebox.showinfo("Success", "Schedule generated successfully!")

    def _copy_schedule(self):
        if not self._last_schedule:
            messagebox.showinfo("Info", "Generate a schedule first.")
            return
        text = format_schedule(self._last_schedule)
        self.root.clipboard_clear()
        self.root.clipboard_append(text)
        messagebox.showinfo("Copied", "Schedule copied to clipboard.")

    def run(self):
        self.root.mainloop()


def main():
    app = EmployeeSchedulerApp()
    app.run()


if __name__ == "__main__":
    main()
