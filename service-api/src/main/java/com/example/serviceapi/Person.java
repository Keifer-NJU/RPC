package com.example.serviceapi;

import lombok.*;

import java.io.Serializable;

/**
 * @author Keifer
 * @createTime 2021/3/9 16:43
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Person implements Serializable {
    public static final long serialVersionUID = 1L;
    private String name;
    private int age;
    private String description;
}
