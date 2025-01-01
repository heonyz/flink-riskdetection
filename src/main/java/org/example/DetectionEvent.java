package org.example;

public class DetectionEvent {

    public long factoryId;
    public String defectType;
    public boolean isDefective;
    public double confidenceScore;
    public long inspectionTime;

    public long getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(long factoryId) {
        this.factoryId = factoryId;
    }

    public String getDefectType() {
        return defectType;
    }

    public void setDefectType(String defectType) {
        this.defectType = defectType;
    }

    public boolean isDefective() {
        return isDefective;
    }

    public void setDefective(boolean defective) {
        isDefective = defective;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public long getInspectionTime() {
        return inspectionTime;
    }

    public void setInspectionTime(long inspectionTime) {
        this.inspectionTime = inspectionTime;
    }
}

