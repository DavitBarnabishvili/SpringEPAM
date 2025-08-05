package com.gym.crm.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

@Entity
@Table(name = "Trainee")
public class Trainee extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "UserId", nullable = false)
    private Long userId;

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

    // Full constructor - for internal use
    public Trainee(Long userId, String firstName, String lastName, String username,
                   String password, Boolean isActive, LocalDate dateOfBirth, String address) {
        super(firstName, lastName, username, password, isActive);
        setUserId(userId);
        setDateOfBirth(dateOfBirth);
        setAddress(address);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
        Trainee trainee = (Trainee) o;

        if (this.getId() != null && trainee.getId() != null) {
            return Objects.equals(getId(), trainee.getId());
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }

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