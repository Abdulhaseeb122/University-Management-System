package com.ums.service.impl;

import com.ums.dto.CourseSectionRequest;
import com.ums.dto.CourseSectionResponse;
import com.ums.entity.AcademicTerm;
import com.ums.entity.Course;
import com.ums.entity.CourseSection;
import com.ums.entity.Faculty;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.AcademicTermRepository;
import com.ums.repository.CourseRepository;
import com.ums.repository.CourseSectionRepository;
import com.ums.repository.FacultyRepository;
import com.ums.service.CourseSectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseSectionServiceImpl implements CourseSectionService {

    private final CourseSectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final AcademicTermRepository termRepository;
    private final FacultyRepository facultyRepository;

    public CourseSectionServiceImpl(CourseSectionRepository sectionRepository,
                                    CourseRepository courseRepository,
                                    AcademicTermRepository termRepository,
                                    FacultyRepository facultyRepository) {
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.termRepository = termRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    @Transactional
    public CourseSectionResponse createSection(CourseSectionRequest request) {
        // 1. Validate course
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with ID: " + request.getCourseId()));

        // 2. Validate term
        AcademicTerm term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic term not found with ID: " + request.getTermId()));

        // 3. Validate faculty (optional)
        Faculty faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Faculty not found with ID: " + request.getFacultyId()));
        }

        // 4. Uniqueness: same course + term cannot have duplicate section name
        if (sectionRepository.existsByCourseIdAndTermIdAndSectionName(
                request.getCourseId(), request.getTermId(), request.getSectionName())) {
            throw new BadRequestException(
                    "Section '" + request.getSectionName() + "' already exists for this course in this term.");
        }

        // 5. Create section
        CourseSection section = new CourseSection();
        section.setCourse(course);
        section.setTerm(term);
        section.setSectionName(request.getSectionName());
        section.setFaculty(faculty);
        section.setMaxCapacity(request.getMaxCapacity());
        section.setCurrentEnrollment(0);

        CourseSection saved = sectionRepository.save(section);
        return mapToResponse(saved);
    }

    @Override
    public List<CourseSectionResponse> getAllSections() {
        return sectionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CourseSectionResponse getSectionById(Long id) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + id));
        return mapToResponse(section);
    }

    @Override
    @Transactional
    public CourseSectionResponse updateSection(Long id, CourseSectionRequest request) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + id));

        // Validate course change
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with ID: " + request.getCourseId()));

        // Validate term change
        AcademicTerm term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic term not found with ID: " + request.getTermId()));

        // Validate faculty if provided
        Faculty faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Faculty not found with ID: " + request.getFacultyId()));
        }

        // Uniqueness check only if section name changed OR course/term changed
        boolean nameChanged = !section.getSectionName().equals(request.getSectionName());
        boolean courseChanged = !section.getCourse().getId().equals(request.getCourseId());
        boolean termChanged = !section.getTerm().getId().equals(request.getTermId());

        if ((nameChanged || courseChanged || termChanged)
                && sectionRepository.existsByCourseIdAndTermIdAndSectionName(
                request.getCourseId(), request.getTermId(), request.getSectionName())) {
            throw new BadRequestException(
                    "Section '" + request.getSectionName() + "' already exists for this course in this term.");
        }

        // Safety: cannot lower maxCapacity below currentEnrollment
        if (request.getMaxCapacity() < section.getCurrentEnrollment()) {
            throw new BadRequestException(
                    "Cannot reduce max capacity below current enrollment (" + section.getCurrentEnrollment() + ").");
        }

        section.setCourse(course);
        section.setTerm(term);
        section.setSectionName(request.getSectionName());
        section.setFaculty(faculty);
        section.setMaxCapacity(request.getMaxCapacity());

        CourseSection updated = sectionRepository.save(section);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + id));

        // Safety: cannot delete section with enrolled students
        if (section.getCurrentEnrollment() != null && section.getCurrentEnrollment() > 0) {
            throw new BadRequestException(
                    "Cannot delete section: " + section.getCurrentEnrollment() + " student(s) are still enrolled.");
        }

        sectionRepository.delete(section);
    }

    @Override
    public List<CourseSectionResponse> getSectionsByTerm(Long termId) {
        if (!termRepository.existsById(termId)) {
            throw new ResourceNotFoundException("Academic term not found with ID: " + termId);
        }
        return sectionRepository.findByTermId(termId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseSectionResponse> getSectionsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        return sectionRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseSectionResponse> getSectionsByFaculty(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResourceNotFoundException("Faculty not found with ID: " + facultyId);
        }
        return sectionRepository.findByFacultyId(facultyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------- Helper Mapper ----------------
    private CourseSectionResponse mapToResponse(CourseSection s) {
        CourseSectionResponse.CourseSectionResponseBuilder builder = CourseSectionResponse.builder()
                .id(s.getId())
                .sectionName(s.getSectionName())
                .maxCapacity(s.getMaxCapacity())
                .currentEnrollment(s.getCurrentEnrollment())
                .availableSeats(s.getMaxCapacity() - s.getCurrentEnrollment())
                .courseId(s.getCourse().getId())
                .courseCode(s.getCourse().getCode())
                .courseTitle(s.getCourse().getTitle())
                .credits(s.getCourse().getCredits())
                .termId(s.getTerm().getId())
                .termName(s.getTerm().getName())
                .termCode(s.getTerm().getTermCode());

        if (s.getFaculty() != null) {
            builder.facultyId(s.getFaculty().getId())
                    .facultyFullName(s.getFaculty().getUser().getFirstName() + " "
                            + s.getFaculty().getUser().getLastName())
                    .facultyEmployeeId(s.getFaculty().getEmployeeId());
        }

        return builder.build();
    }
}