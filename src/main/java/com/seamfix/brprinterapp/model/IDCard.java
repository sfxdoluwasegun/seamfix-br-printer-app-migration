package com.seamfix.brprinterapp.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Table;

import javax.persistence.Entity;
import java.sql.Timestamp;

@Entity
@Table(name = "IDCARD")
@Getter
@Setter
@NoArgsConstructor
public class IDCard extends BaseEntity {

    @Column(unique = true)
    private String systemId;

    @Column
    private Timestamp createTimeStamp;


    @Column
    private int timesPrinted;

}
