package com.tds.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Entity
@Table(name = ExplorerMessage.TABLE_ARTICLE)
public class ExplorerMessage {
    static final String TABLE_ARTICLE = "MAIL";

    static final String COLUMN_ID = "id";

    static final String COLUMN_IP = "ip";

    static final String COLUMN_PORT = "port";

    static final String COLUMN_TYPE = "type";

    @Column(name = COLUMN_ID)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = COLUMN_IP)
    private String ip;
    @Column(name = COLUMN_PORT)
    private String port;
    @Column(name = COLUMN_TYPE)
    private String type;
}
