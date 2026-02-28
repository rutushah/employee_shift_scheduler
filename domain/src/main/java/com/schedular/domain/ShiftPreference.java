package com.schedular.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "shift_preference",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "day_name", "pref_rank"})
)
public class ShiftPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="employee_id", nullable=false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_name", nullable=false)
    private Day day;

    @Column(name = "pref_rank", nullable=false)
    private int rank;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_name", nullable=false)
    private Shift shift;

    public ShiftPreference() {}

    public ShiftPreference(Employee employee, Day day, int rank, Shift shift) {
        this.employee = employee;
        this.day = day;
        this.rank = rank;
        this.shift = shift;
    }

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public Day getDay() { return day; }
    public int getRank() { return rank; }
    public Shift getShift() { return shift; }

    public void setId(Long id) { this.id = id; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public void setDay(Day day) { this.day = day; }
    public void setRank(int rank) { this.rank = rank; }
    public void setShift(Shift shift) { this.shift = shift; }
}