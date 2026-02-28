package com.schedular.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "shift_assignment")
public class ShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="employee_id", nullable=false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name="day_name", nullable=false)
    private Day day;

    @Enumerated(EnumType.STRING)
    @Column(name="shift_name", nullable=false)
    private Shift shift;

    public ShiftAssignment() {}

    public ShiftAssignment(Employee employee, Day day, Shift shift) {
        this.employee = employee;
        this.day = day;
        this.shift = shift;
    }


    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public Day getDay() { return day; }
    public Shift getShift() { return shift; }

    public void setId(Long id) { this.id = id; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public void setDay(Day day) { this.day = day; }
    public void setShift(Shift shift) { this.shift = shift; }
}