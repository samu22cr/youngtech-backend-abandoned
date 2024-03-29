package com.youngtechcr.www.person;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.youngtechcr.www.security.user.User;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tbl_person")
@JsonDeserialize(builder = Person.Builder.class)
public class Person  {

    @Id
    @Column(name = "id_person")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    @JoinColumn(name = "fk_id_user", referencedColumnName = "id_user")
    private User user;
    private String firstnames;
    private String lastnames;
    private int age;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthdate;
    public Person() { }
    private Person(
            Integer id,
            User user,
            String firstnames,
            String lastnames,
            int age,
            LocalDate birthdate
    ) {
        this.id = id;
        this.user = user;
        this.firstnames = firstnames;
        this.lastnames = lastnames;
        this.age = age;
        this.birthdate = birthdate;
    }

    public Person(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    @JsonBackReference("user-person")
    public User getUser() {
        return user;
    }

    public String getFirstnames() {
        return firstnames;
    }


    public String getLastnames() {
        return lastnames;
    }

    public int getAge() {
        return age;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (age != person.age) return false;
        if (!Objects.equals(id, person.id)) return false;
        if (!Objects.equals(user, person.user)) return false;
        if (!Objects.equals(firstnames, person.firstnames)) return false;
        if (!Objects.equals(lastnames, person.lastnames)) return false;
        return Objects.equals(birthdate, person.birthdate);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (firstnames != null ? firstnames.hashCode() : 0);
        result = 31 * result + (lastnames != null ? lastnames.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (birthdate != null ? birthdate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Person{" +
                "personId=" + id +
                ", firstname='" + firstnames + '\'' +
                ", lastname='" + lastnames + '\'' +
                ", age=" + age +
                ", birthdate=" + birthdate +
                '}';
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private Integer id;
        private User user;
        private String firstnames;
        private String lastnames;
        private int age;
        private LocalDate birthdate;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder firstnames(String firstname) {
            this.firstnames = firstname;
            return this;
        }

        public Builder lastnames(String lastname) {
            this.lastnames = lastname;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder birthdate(LocalDate birthdate) {
            this.birthdate = birthdate;
            return this;
        }

        public Person build() {
            return new Person(
                this.id,
                this.user,
                this.firstnames,
                this.lastnames,
                this.age,
                this.birthdate
            );
        }
    }
}
