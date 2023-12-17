package com.workintech.s17challange.controller;

import com.workintech.s17challange.entity.ApiResponse;
import com.workintech.s17challange.entity.Course;
import com.workintech.s17challange.entity.CourseGpa;
import com.workintech.s17challange.exception.ApiException;
import com.workintech.s17challange.validation.CourseValidation;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/courses")
public class CourseController {
    private List<Course> courses;
    private CourseGpa lowCourseGpa;
    private CourseGpa mediumCourseGpa;
    private CourseGpa highCourseGpa;

    @PostConstruct
    public void init() {
        this.courses = new ArrayList<>();
    }

    @Autowired
    public CourseController(
            @Qualifier("lowCourseGpa") CourseGpa lowCourseGpa,
            @Qualifier("mediumCourseGpa") CourseGpa mediumCourseGpa,
            @Qualifier("highCourseGpa") CourseGpa highCourseGpa
    ) {
        this.lowCourseGpa = lowCourseGpa;
        this.mediumCourseGpa = mediumCourseGpa;
        this.highCourseGpa = highCourseGpa;
    }

    @GetMapping
    public List<Course> getAll() {
        return this.courses;
    }

    @GetMapping("/{name}")
    public Course getByName(@PathVariable("name") String name) {
        CourseValidation.checkName(name);
        return courses.stream()
                .filter(course -> course.getName().equalsIgnoreCase(name))
                .findFirst().orElseThrow(() -> new ApiException("Course not found with name " + name, HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody Course course) {
        CourseValidation.checkCredit(course.getCredit());
        CourseValidation.checkName(course.getName());
        courses.add(course);
        int totalGpa = getTotalGpa(course);
        ApiResponse apiResponse = new ApiResponse(course, totalGpa);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    private int getTotalGpa(Course course) {
        if (course.getCredit() <= 2) {
            return course.getGrade().getCoefficient() * course.getCredit() * lowCourseGpa.getGpa();
        } else if (course.getCredit() == 3) {
            return course.getGrade().getCoefficient() * course.getCredit() * mediumCourseGpa.getGpa();
        } else {
            return course.getGrade().getCoefficient() * course.getCredit() * highCourseGpa.getGpa();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable("id") int id, @RequestBody Course course) {
        CourseValidation.checkId(id);
        CourseValidation.checkCredit(course.getCredit());
        CourseValidation.checkName(course.getName());
        Course existingCourse = courses.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ApiException("Course not found with id: " + id, HttpStatus.BAD_REQUEST));
        int indexOfExistingCourse = courses.indexOf(existingCourse);
        course.setId(id);
        courses.set(indexOfExistingCourse, course);
        int totalGpa = getTotalGpa(course);
        ApiResponse apiResponse = new ApiResponse(courses.get(indexOfExistingCourse), totalGpa);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int id){
        Course existingCourse = courses.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ApiException("Course not found with id: "+id, HttpStatus.BAD_REQUEST));
        courses.remove(existingCourse);
    }
}

