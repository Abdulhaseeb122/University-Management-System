package com.ums.service.impl;

import com.ums.dto.StudentProfileResponse;
import com.ums.dto.StudentUpdateRequest;
import com.ums.entity.Student;
import com.ums.entity.User;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.StudentRepository;
import com.ums.repository.UserRepository;
import com.ums.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    // Updated constructor with UserRepository
    public StudentServiceImpl(StudentRepository studentRepository,
                              UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public StudentProfileResponse getStudentProfile(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for email: " + email));

        return mapToResponse(student);
    }

    // ---------------- NEW METHOD ----------------
    @Override
    @Transactional
    public StudentProfileResponse updateStudentProfile(String email, StudentUpdateRequest request) {
        // 1. Find the student
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for email: " + email));

        User user = student.getUser();

        // 2. Check if phone number is already taken by another user
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered by another user!");
        }

        // 3. Update User fields
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        // 4. Update Student field (only if provided)
        if (request.getCurrentSemester() != null) {
            // Basic validation: semester must be between 1 and 12
            if (request.getCurrentSemester() < 1 || request.getCurrentSemester() > 12) {
                throw new BadRequestException("Semester must be between 1 and 12");
            }
            student.setCurrentSemester(request.getCurrentSemester());
        }

        // 5. Save both entities
        userRepository.save(user);
        Student updatedStudent = studentRepository.save(student);

        // 6. Return updated profile
        return mapToResponse(updatedStudent);
    }

    // -------- Helper Mapper (unchanged) --------
    private StudentProfileResponse mapToResponse(Student student) {
        User user = student.getUser();

        return StudentProfileResponse.builder()
                .id(student.getId())
                .rollNumber(student.getRollNumber())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .departmentName(student.getDepartment().getName())
                .currentSemester(student.getCurrentSemester())
                .admissionDate(student.getAdmissionDate())
                .academicStatus(student.getAcademicStatus())
                .cgpa(student.getCgpa())
                .totalCreditsEarned(student.getTotalCreditsEarned())
                .build();
    }
}