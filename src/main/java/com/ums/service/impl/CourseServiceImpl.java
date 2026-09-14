package com.ums.service.impl;

import com.ums.dto.CourseRequest;
import com.ums.dto.CourseResponse;
import com.ums.entity.Course;
import com.ums.entity.Department;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.CourseRepository;
import com.ums.repository.CourseSectionRepository;
import com.ums.repository.DepartmentRepository;
import com.ums.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseSectionRepository courseSectionRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             DepartmentRepository departmentRepository,
                             CourseSectionRepository courseSectionRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.courseSectionRepository = courseSectionRepository;
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        // 1. Validate department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + request.getDepartmentId()));

        // 2. Uniqueness check on course code
        if (courseRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Course code already exists: " + request.getCode());
        }

        // 3. Create Course entity and apply defaults
        Course course = new Course();
        course.setDepartment(department);
        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLectureHours(request.getLectureHours() != null ? request.getLectureHours() : 3);
        course.setLabHours(request.getLabHours() != null ? request.getLabHours() : 0);
        course.setCredits(request.getCredits());
        course.setIsElective(request.getIsElective() != null ? request.getIsElective() : false);

        Course saved = courseRepository.save(course);
        return mapToResponse(saved);
    }

    @Override
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));
        return mapToResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));

        // Validate department exists (may change)
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + request.getDepartmentId()));

        // If course code is being changed, verify uniqueness
        if (!course.getCode().equals(request.getCode())
                && courseRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Course code already exists: " + request.getCode());
        }

        course.setDepartment(department);
        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setLectureHours(request.getLectureHours() != null ? request.getLectureHours() : course.getLectureHours());
        course.setLabHours(request.getLabHours() != null ? request.getLabHours() : course.getLabHours());
        course.setCredits(request.getCredits());
        course.setIsElective(request.getIsElective() != null ? request.getIsElective() : course.getIsElective());

        Course updated = courseRepository.save(course);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));

        // Safety: prevent deletion if course has any sections
        if (courseSectionRepository.existsByCourseId(id)) {
            throw new BadRequestException(
                    "Cannot delete course: it has active sections. Delete the sections first.");
        }

        courseRepository.delete(course);
    }

    @Override
    public List<CourseResponse> getCoursesByDepartment(Long departmentId) {
        // Validate department exists first
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }

        return courseRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------- Helper Mapper ----------------
    private CourseResponse mapToResponse(Course c) {
        return CourseResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .title(c.getTitle())
                .description(c.getDescription())
                .lectureHours(c.getLectureHours())
                .labHours(c.getLabHours())
                .credits(c.getCredits())
                .isElective(c.getIsElective())
                .departmentId(c.getDepartment().getId())
                .departmentName(c.getDepartment().getName())
                .departmentCode(c.getDepartment().getCode())
                .build();
    }
}