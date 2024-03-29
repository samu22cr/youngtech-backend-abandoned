package com.youngtechcr.www.school.student;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.youngtechcr.www.customer.Customer;
import com.youngtechcr.www.domain.Timestamped;
import com.youngtechcr.www.school.School;
import com.youngtechcr.www.school.group.SchoolGroup;
import com.youngtechcr.www.security.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tbl_student")
public class Student implements Timestamped {

    @Id
    @Column(name = "id_student")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn( name = "fk_id_school",
    referencedColumnName = "id_school" )
    private School school;

    @ManyToOne
    @JoinColumn(name = "fk_id_school_group",
    referencedColumnName = "id_school_group")
    private SchoolGroup group;

    @OneToOne
    @JoinColumn(name = "fk_id_user",
    referencedColumnName = "id_user")
    private User user;

    public Student() { }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonBackReference("school-students")
    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    @JsonBackReference("group-students")
    public SchoolGroup getGroup() {
        return group;
    }

    public void setGroup(SchoolGroup group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;

        if (!Objects.equals(id, student.id)) return false;
        if (!Objects.equals(createdAt, student.createdAt)) return false;
        if (!Objects.equals(updatedAt, student.updatedAt)) return false;
        if (!Objects.equals(school, student.school)) return false;
        if (!Objects.equals(group, student.group)) return false;
        return Objects.equals(user, student.user);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        result = 31 * result + (school != null ? school.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", school=" + school +
                ", group=" + group +
                ", user=" + user +
                '}';
    }
}












