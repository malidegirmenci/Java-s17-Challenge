package com.workintech.s17challange.entity;

import lombok.Data;
import org.springframework.stereotype.Component;
@Data
@Component
public class Course {
    private Integer id;
    private String name;
    private Integer credit;
    private Grade grade;
}
