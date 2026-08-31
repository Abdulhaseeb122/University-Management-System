package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "hostel_rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class HostelRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(name = "room_number", nullable = false, length = 10)
    private String roomNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "monthly_rent", nullable = false, precision = 8, scale = 2)
    private BigDecimal monthlyRent;
}