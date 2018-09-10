package com.seamfix.brprinterapp.model;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Table;

import javax.persistence.Entity;
import java.sql.Timestamp;

@Entity
@Table(name = "IDCARD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdCard extends BaseEntity {

    @Column(unique = true)
    private String systemId;

    @Column
    private Timestamp latestTime;

    @Column
    private long timesPrinted;

}
