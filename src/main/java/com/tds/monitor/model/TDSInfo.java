package com.tds.monitor.model;

import lombok.Data;

@Data
public class TDSInfo {
    private String cpu;
    private String memoryUsed;
    private boolean mining;
    private String minings;
    private String totalMemory;
    private String memory;


    private String currentDifficulty;
    private String averageBlockInterval;
    private String averageGasPrice;
    private String transactionPoolSize;
    private String height;
    private String blocksPerDay;

    private String p2pAddress;

    public String getMemory() {
        return (memory != null) ? memory : "";
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getMinings() {
        return minings;
    }

    public void setMinings(String minings) {
        this.minings = minings;
    }

    public boolean isMining() {
        return mining;
    }

    public void setMining(boolean mining) {
        this.mining = mining;
    }

    public String getTotalMemory() {
        return (totalMemory != null) ? totalMemory : "";
    }

    public void setTotalMemory(String totalMemory) {
        this.totalMemory = totalMemory;
    }

    public String getCpu() {
        return (cpu != null) ? cpu : "";
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getCurrentDifficulty() {
        return (currentDifficulty != null) ? currentDifficulty : "";
    }

    public void setCurrentDifficulty(String currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }

    public String getAverageBlockInterval() {
        return (averageBlockInterval != null) ? averageBlockInterval : "0";
    }

    public void setAverageBlockInterval(String averageBlockInterval) {
        this.averageBlockInterval = averageBlockInterval;
    }

    public String getAverageGasPrice() {
        return (averageGasPrice != null) ? averageGasPrice : "";
    }

    public void setAverageGasPrice(String averageGasPrice) {
        this.averageGasPrice = averageGasPrice;
    }

    public String getMemoryUsed() {
        return (memoryUsed != null) ? memoryUsed : "0";
    }

    public void setMemoryUsed(String memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public String getTransactionPoolSize() {
        return (transactionPoolSize != null) ? transactionPoolSize : "0";
    }

    public void setTransactionPoolSize(String transactionPoolSize) {
        this.transactionPoolSize = transactionPoolSize;
    }

    public String getHeight() {
        return (height != null) ? height : "0";
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getBlocksPerDay() {
        return (blocksPerDay != null) ? blocksPerDay : "0";
    }

    public void setBlocksPerDay(String blocksPerDay) {
        this.blocksPerDay = blocksPerDay;
    }
}
