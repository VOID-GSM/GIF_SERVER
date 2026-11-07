package com.example.gifserverv2.domain.student.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자가 업로드한 학생 명단. 회원가입(DataGSM 로그인)을 하지 않은 학생을 찾는 기준이 된다.
 * 학번(예: 1101)으로 users 와 맞춰 본다.
 */
@Entity
@Table(name = "student_roster")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentRoster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", nullable = false, unique = true, length = 10)
    private String studentNumber;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer grade;

    @Column(name = "class_no", nullable = false)
    private Integer classNo;

    @Column(name = "student_no", nullable = false)
    private Integer studentNo;

    public StudentRoster(String studentNumber, String name) {
        this.studentNumber = studentNumber;
        update(name);
    }

    public void update(String name) {
        this.name = name;
        this.grade = Character.getNumericValue(studentNumber.charAt(0));
        this.classNo = Character.getNumericValue(studentNumber.charAt(1));
        this.studentNo = Integer.parseInt(studentNumber.substring(2));
    }
}
