package com.ums.service.impl;

import com.ums.dto.FacultyRequest;
import com.ums.dto.FacultyResponse;
import com.ums.entity.Department;
import com.ums.entity.Faculty;
import com.ums.entity.Role;
import com.ums.entity.User;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.DepartmentRepository;
import com.ums.repository.FacultyRepository;
import com.ums.repository.RoleRepository;
import com.ums.repository.UserRepository;
import com.ums.service.FacultyService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyServiceImpl(FacultyRepository facultyRepository,
                              UserRepository userRepository,
                              RoleRepository roleRepository,
                              DepartmentRepository departmentRepository,
                              PasswordEncoder passwordEncoder) {
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public FacultyResponse createFaculty(FacultyRequest request) {
        // 1. Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        // 2. Validate phone uniqueness (if provided)
        if (request.getPhoneNumber() != null
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered: " + request.getPhoneNumber());
        }

        // 3. Validate employeeId uniqueness
        if (facultyRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists: " + request.getEmployeeId());
        }

        // 4. Fetch ROLE_FACULTY (must exist in roles table)
        Role facultyRole = roleRepository.findByName("ROLE_FACULTY")
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_FACULTY not found in database"));

        // 5. Fetch the department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + request.getDepartmentId()));

        // 6. Create and save the User
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(facultyRole);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // 7. Create Faculty profile (shares PK with User via @MapsId)
        Faculty faculty = new Faculty();
        faculty.setId(savedUser.getId());
        faculty.setUser(savedUser);
        faculty.setEmployeeId(request.getEmployeeId());
        faculty.setDepartment(department);
        faculty.setDesignation(request.getDesignation());
        faculty.setJoiningDate(request.getJoiningDate());
        faculty.setOfficeRoomNumber(request.getOfficeRoomNumber());
        faculty.setSpecialization(request.getSpecialization());

        Faculty savedFaculty = facultyRepository.save(faculty);
        return mapToResponse(savedFaculty);
    }

    @Override
    public List<FacultyResponse> getAllFaculty() {
        return facultyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FacultyResponse getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + id));
        return mapToResponse(faculty);
    }

    @Override
    @Transactional
    public FacultyResponse updateFaculty(Long id, FacultyRequest request) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + id));
        User user = faculty.getUser();

        // Validate email uniqueness (if changing)
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        // Validate phone uniqueness (if changing)
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered: " + request.getPhoneNumber());
        }

        // Validate employeeId uniqueness (if changing)
        if (!faculty.getEmployeeId().equals(request.getEmployeeId())
                && facultyRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists: " + request.getEmployeeId());
        }

        // Validate department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + request.getDepartmentId()));

        // Update User fields (NOTE: we do NOT update password here - separate endpoint later)
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(user);

        // Update Faculty fields
        faculty.setEmployeeId(request.getEmployeeId());
        faculty.setDepartment(department);
        faculty.setDesignation(request.getDesignation());
        faculty.setJoiningDate(request.getJoiningDate());
        faculty.setOfficeRoomNumber(request.getOfficeRoomNumber());
        faculty.setSpecialization(request.getSpecialization());

        Faculty updated = facultyRepository.save(faculty);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + id));

        // Note: Deleting the User cascades to Faculty via FK (ON DELETE CASCADE)
        // We delete the User to remove both records cleanly.
        userRepository.delete(faculty.getUser());
    }

    @Override
    public List<FacultyResponse> getFacultyByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }
        return facultyRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------- Helper Mapper ----------------
    private FacultyResponse mapToResponse(Faculty f) {
        User u = f.getUser();
        return FacultyResponse.builder()
                .id(f.getId())
                .employeeId(f.getEmployeeId())
                .fullName(u.getFirstName() + " " + u.getLastName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .gender(u.getGender())
                .dateOfBirth(u.getDateOfBirth())
                .departmentName(f.getDepartment().getName())
                .departmentCode(f.getDepartment().getCode())
                .designation(f.getDesignation())
                .joiningDate(f.getJoiningDate())
                .officeRoomNumber(f.getOfficeRoomNumber())
                .specialization(f.getSpecialization())
                .build();
    }
}