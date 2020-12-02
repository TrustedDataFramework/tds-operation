package com.tds.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class Meaasge {
    private String ip;
    private String port;
    private String type;
    private String registerCode;
}
