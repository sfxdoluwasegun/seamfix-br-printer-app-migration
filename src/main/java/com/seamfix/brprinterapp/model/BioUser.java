package com.seamfix.brprinterapp.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;


@Entity
@Table(name = "BIO_USER")
@Getter
@Setter
@NoArgsConstructor
public class BioUser extends BaseEntity {

    @Column(unique = true)
    private String email;

    @Column
    private String pw;

    @Column
    private String phone;

    @Column
    private String fName;

    @Column
    private String lName;

    @Column
    private String mName;

    @Column
    private boolean active;

    @Column
    private String gender;

    @Column
    private String companyName;

    @Column
    private String userType;

    @Column
    private boolean passwordReset;

    @Column
    private Boolean gdprCompliant;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Project> projects;

}
