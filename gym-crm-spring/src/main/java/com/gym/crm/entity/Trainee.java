package com.gym.crm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

@Entity
@Table(name = "Trainee")
public class Trainee extends User {  // JOINED inheritance

    @Column(name = "Date of Birth")
    private LocalDate dateOfBirth;

    @Column(name = "Address")
    private String address;

    public Trainee() {
        super();
    }

    public Trainee(String firstName, String lastName) {
        super(firstName, lastName);
    }

    public Trainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        super(firstName, lastName);
        setDateOfBirth(dateOfBirth);
        setAddress(address);
    }

    public Trainee(String firstName, String lastName, String username,
                   String password, Boolean isActive, LocalDate dateOfBirth, String address) {
        super(firstName, lastName, username, password, isActive);
        setDateOfBirth(dateOfBirth);
        setAddress(address);
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address != null ? address.trim() : null;
    }

    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean hasDateOfBirth() {
        return dateOfBirth != null;
    }

    public boolean hasAddress() {
        return address != null && !address.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Trainee{" +
                "firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                ", age=" + getAge() +
                ", isActive=" + getIsActive() +
                '}';
    }
}