package com.siemens.internship.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Clean validation of non-empty name
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private String status;

    // Clean validation of non-empty name
    // Clean annotation for email validation, a custom regex could have been used, but it would have been more difficult and risky to implement correctly
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
}