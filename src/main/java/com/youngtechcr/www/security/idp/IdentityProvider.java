package com.youngtechcr.www.security.idp;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youngtechcr.www.security.user.User;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tbl_identity_provider")
public class IdentityProvider {

    @Id
    @Column(name = "id_identity_provider")
    private Integer id;
    private String name;

    @OneToMany(mappedBy = "identityProvider", fetch = FetchType.EAGER)
    private List<User> users;

    public Integer getId() {
        return id;
    }

    public IdentityProvider setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public IdentityProvider setName(String name) {
        this.name = name;
        return this;
    }

    @JsonBackReference("user-identity_provider")
    public List<User> getUsers() {
        return users;
    }

    public IdentityProvider setUsers(List<User> users) {
        this.users = users;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdentityProvider that = (IdentityProvider) o;

        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (users != null ? users.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IdentityProvider{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}