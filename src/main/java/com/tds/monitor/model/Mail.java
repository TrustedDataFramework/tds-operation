package com.tds.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Entity
@Table(name = Mail.TABLE_ARTICLE)
public class Mail {
    static final String TABLE_ARTICLE = "MAIL";

    static final String COLUMN_ID = "id";

    static final String COLUMN_SENDER = "sender";

    static final String COLUMN_RECEIVER = "receiver";

    static final String COLUMN_PASSWORD = "password";

    @Column(name = COLUMN_ID)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = COLUMN_SENDER)
    private String sender;
    @Column(name = COLUMN_RECEIVER)
    private String receiver;
    @Column(name = COLUMN_PASSWORD)
    private String password;
}
