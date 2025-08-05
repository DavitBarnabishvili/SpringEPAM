package com.gym.crm.model;

import java.util.Objects;

public abstract class User {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Boolean isActive;

    public User() {
        this.isActive = true;
    }

    public User(String firstName, String lastName) {
        this();
        setFirstName(firstName);
        setLastName(lastName);
    }

    public User(String firstName, String lastName, String username, String password, Boolean isActive) {
        setFirstName(firstName);
        setLastName(lastName);
        setUsername(username);
        setPassword(password);
        setIsActive(isActive);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName.trim() : null;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName.trim() : null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive != null ? isActive : true;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) return "Unknown User";
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;

        return Objects.equals(username, user.username) && username != null;
    }

    @Override
    public int hashCode() {
        if (username != null) {
            return username.hashCode();
        }
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}