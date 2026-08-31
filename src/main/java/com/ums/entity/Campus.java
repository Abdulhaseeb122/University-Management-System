package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;
}