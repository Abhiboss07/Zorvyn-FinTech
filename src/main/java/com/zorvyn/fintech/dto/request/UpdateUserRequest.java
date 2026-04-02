package com.zorvyn.fintech.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    // ── Getters & Setters ──

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
