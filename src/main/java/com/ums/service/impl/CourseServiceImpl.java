package com.ums.service.impl;

import com.ums.entity.*;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.*;
import com.ums.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             CourseSectionRepository courseSectionRepository,
                             EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllAvailableCourses() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> getMyCourses(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return enrollmentRepository.findByStudentId(student.getId());
    }

    @Override
    @Transactional
    public String enrollStudent(String email, Long sectionId) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        CourseSection section = courseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Course Section not found"));

        // Duplicate Check (Database schema ke mutabiq)
        if (enrollmentRepository.existsByStudentIdAndSectionId(student.getId(), sectionId)) {
            throw new BadRequestException("Already enrolled in this section!");
        }

        // Capacity Check (Section ki capacity)
        if (section.getCurrentEnrollment() >= section.getMaxCapacity()) {
            throw new BadRequestException("Section is full!");
        }

        // Enrollment Save
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollmentRepository.save(enrollment);

        // Section enrollment count update karein
        section.setCurrentEnrollment(section.getCurrentEnrollment() + 1);
        courseSectionRepository.save(section);

        return "Successfully enrolled in " + section.getCourse().getTitle() + " - Section " + section.getSectionName();
    }
}