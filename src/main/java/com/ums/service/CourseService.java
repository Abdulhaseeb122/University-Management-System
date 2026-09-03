package com.ums.service;
import com.ums.entity.Course;
import com.ums.entity.Enrollment;
import java.util.List;
public interface CourseService {
    List<Course> getAllAvailableCourses();
    List<Enrollment> getMyCourses(String email);
    String enrollStudent(String email, Long sectionId);
}