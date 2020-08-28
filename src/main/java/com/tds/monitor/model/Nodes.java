package com.tds.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Entity
@Table(name = Nodes.TABLE_ARTICLE)
public class Nodes {

    static final String TABLE_ARTICLE = "node";

    static final String COLUMN_ID = "id";

    static final String COLUMN_NODEIP = "nodeip";

    static final String COLUMN_NODEPORT = "nodePort";

    static final String COLUMN_NODETYPE = "nodeType";

    static final String COLUMN_NODESTATE = "nodeState";

    static final String COLUMN_NODEVERSION = "nodeVersion";

    static final String COLUMN_USERNAME = "userName";

    static final String COLUMN_PASSWORD = "password";

    static final String COLUMN_LEVELDBPATH = "leveldbPath";

    @Column(name = COLUMN_ID)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = COLUMN_NODEIP)
    private String nodeIP;
    @Column(name = COLUMN_NODEPORT)
    private String nodePort;
    @Column(name = COLUMN_NODETYPE)
    private String nodeType;
    @Column(name = COLUMN_NODESTATE)
    private String nodeState;
    @Column(name = COLUMN_NODEVERSION)
    private String nodeVersion;
    @Column(name = COLUMN_USERNAME)
    private String userName;
    @Column(name = COLUMN_PASSWORD)
    private String password;
    @Column(name = COLUMN_LEVELDBPATH)
    private String leveldbPath;

}
