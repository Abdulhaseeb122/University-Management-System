package com.ums.service.impl;

import com.ums.dto.StudentProfileResponse;
import com.ums.entity.Student;
import com.ums.entity.User;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.StudentRepository;
import com.ums.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileResponse getStudentProfile(String email) {
        // Find student using the user's email
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for email: " + email));

        return mapToResponse(student);
    }

    // Helper method to convert Student entity to DTO
    private StudentProfileResponse mapToResponse(Student student) {
        User user = student.getUser();

        String departmentName = (student.getDepartment() != null)
                ? student.getDepartment().getName()
                : "Unassigned";

        return StudentProfileResponse.builder()
                .id(student.getId())
                .rollNumber(student.getRollNumber())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .departmentName(departmentName)
                .currentSemester(student.getCurrentSemester())
                .admissionDate(student.getAdmissionDate())
                .academicStatus(student.getAcademicStatus())
                .cgpa(student.getCgpa())
                .totalCreditsEarned(student.getTotalCreditsEarned())
                .build();
    }
}