package com.tds.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Entity
@Table(name = User.TABLE_ARTICLE)
public class User {

    static final String TABLE_ARTICLE = "user";

    static final String COLUMN_ID = "id";

    static final String COLUMN_NAME = "name";

    static final String COLUMN_PASSWORD = "password";

    static final String COLUMN_ROLE = "role";

    static final String COLUMN_LOGIN_TIME = "login_time";


    @Column(name = COLUMN_ID)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = COLUMN_NAME)
    private String name;
    @Column(name = COLUMN_PASSWORD)
    private String password;
    @Column(name = COLUMN_ROLE)
    private String role;
    @Column(name = COLUMN_LOGIN_TIME)
    private String login_time;
}
