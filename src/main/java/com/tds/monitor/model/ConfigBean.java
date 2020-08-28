package com.tds.monitor.model;

//@EnableConfigurationProperties(ConfigBean.class)
public class ConfigBean {
    private boolean blockIsStuck;

    public boolean isBlockIsStuck() {
        return blockIsStuck;
    }

    public void setBlockIsStuck(boolean blockIsStuck) {
        this.blockIsStuck = blockIsStuck;
    }
}
