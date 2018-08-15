package com.seamfix.brprinterapp.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.sql.Timestamp;

/**
 * Created by rukevwe on 5/19/2017.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Tag extends BaseEntity {

    @Column
    private String tag;

    @Column
    private String uniqueId;

    @Column
    private Timestamp createTimestamp;
}

