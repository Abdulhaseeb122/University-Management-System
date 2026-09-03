package com.ums.service.impl;

import com.ums.dto.LoginRequest;
import com.ums.dto.RegisterRequest;
import com.ums.dto.StudentResponse;
import com.ums.entity.Role;
import com.ums.entity.Student;
import com.ums.entity.User;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.RoleRepository;
import com.ums.repository.StudentRepository;
import com.ums.repository.UserRepository;
import com.ums.security.JwtUtils;
import com.ums.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           StudentRepository studentRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public StudentResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered!");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);

        User savedUser = userRepository.save(user);

        // Auto-create Student entity if assigned role is STUDENT
        if ("ROLE_STUDENT".equalsIgnoreCase(role.getName()) || "STUDENT".equalsIgnoreCase(role.getName())) {
            Student student = Student.builder()
                    .rollNumber("STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .currentSemester(1)
                    .admissionDate(LocalDate.now())
                    .cgpa(BigDecimal.ZERO)
                    .totalCreditsEarned(0)
                    .academicStatus(Student.AcademicStatus.ACTIVE)
                    .user(savedUser)
                    .build();

            studentRepository.save(student);
        }

        return StudentResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFirstName() + " " + savedUser.getLastName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .build();
    }

    @Override
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password!");
        }

        return jwtUtils.generateToken(user.getEmail());
    }
}