package com.gym.crm.util;

/**
 * Service interface for generating usernames and passwords according to assignment requirements.
 */
public interface CredentialsGeneratorService {

    /**
     * Generates a unique username from first and last name.
     * Format: firstName.lastName (example: Davit.Barnabishvili)
     * If duplicate exists, adds serial number (example: Davit.Barnabishvili1, Davit.Barnabishvili2)
     *
     * @param firstName User's first name
     * @param lastName User's last name
     * @return Unique username
     * @throws IllegalArgumentException if first or last names are null or empty
     */
    String generateUsername(String firstName, String lastName);

    /**
     * Generates a random Password that is 10 characters long.
     *
     * @return generated password
     */
    String generatePassword();

    /**
     * Checks if the username is unique. Necessary for generateUsername to work.
     * @param username to check
     * @return true when username is unique, false when it isn't
     */
    boolean isUsernameUnique(String username);
}