package com.gym.crm.dto.response;

public class RegistrationResponse {
    private String username;
    private String password;

    public RegistrationResponse() {}

    public RegistrationResponse(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}