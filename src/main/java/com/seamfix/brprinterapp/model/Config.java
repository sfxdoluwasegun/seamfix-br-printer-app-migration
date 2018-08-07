package com.seamfix.brprinterapp.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by rukevwe on 8/7/2018
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Config extends BaseEnitiy {

    @Column(unique = true)
    private  String configKey;

    @Column(length = 5000)
    private String configValue;
}
