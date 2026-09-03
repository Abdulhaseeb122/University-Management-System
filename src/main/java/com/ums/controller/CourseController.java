package com.ums.controller;

import com.ums.entity.Course;
import com.ums.entity.Enrollment;
import com.ums.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllAvailableCourses();
    }

    @GetMapping("/my-courses")
    public List<Enrollment> getMyCourses(@AuthenticationPrincipal UserDetails userDetails) {
        return courseService.getMyCourses(userDetails.getUsername());
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<String> enroll(@AuthenticationPrincipal UserDetails userDetails,
                                         @PathVariable Long courseId) {
        String response = courseService.enrollStudent(userDetails.getUsername(), courseId);
        return ResponseEntity.ok(response);
    }
}