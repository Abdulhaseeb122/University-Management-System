package com.ums.service.impl;

import com.ums.dto.LoginRequest;
import com.ums.dto.RegisterRequest;
import com.ums.dto.StudentResponse;
import com.ums.entity.Department;           // <-- NEW IMPORT
import com.ums.entity.Role;
import com.ums.entity.Student;             // <-- NEW IMPORT
import com.ums.entity.User;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.DepartmentRepository; // <-- NEW IMPORT
import com.ums.repository.RoleRepository;
import com.ums.repository.StudentRepository;   // <-- NEW IMPORT
import com.ums.repository.UserRepository;
import com.ums.security.JwtUtils;
import com.ums.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // <-- NEW IMPORT

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;   // <-- NEW FIELD
    private final DepartmentRepository departmentRepository; // <-- NEW FIELD
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // UPDATED CONSTRUCTOR with new dependencies
    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           StudentRepository studentRepository,     // <-- NEW
                           DepartmentRepository departmentRepository, // <-- NEW
                           PasswordEncoder passwordEncoder,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;           // <-- NEW
        this.departmentRepository = departmentRepository;     // <-- NEW
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional
    public StudentResponse registerUser(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        // 2. Check if phone already exists (good practice)
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered!");
        }

        // 3. Fetch the Role
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));

        // 4. --- NEW: If role is STUDENT, fetch the Department ---
        Department department = null;
        if (role.getName().equals("ROLE_STUDENT")) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));
        }

        // 5. Create and save the User
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // BCrypt encryption
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);
        user.setIsActive(true); // <-- Added to ensure user is active

        User savedUser = userRepository.save(user);

        // 6. --- NEW: If role is STUDENT, create and save Student profile ---
        if (role.getName().equals("ROLE_STUDENT") && department != null) {
            Student student = new Student();

            // @MapsId: Student uses the same ID as User
            student.setId(savedUser.getId());
            student.setUser(savedUser);

            // Generate a simple Roll Number (e.g., STU-123456)
            String rollNumber = "STU-" + System.currentTimeMillis() % 1000000;
            student.setRollNumber(rollNumber);

            student.setDepartment(department);
            student.setCurrentSemester(1);
            student.setAdmissionDate(LocalDate.now());
            student.setAcademicStatus(Student.AcademicStatus.ACTIVE);
            student.setCgpa(java.math.BigDecimal.ZERO);
            student.setTotalCreditsEarned(0);

            studentRepository.save(student);
        }

        // 7. Return the response
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