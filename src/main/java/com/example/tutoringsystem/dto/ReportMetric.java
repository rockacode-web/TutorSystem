package com.example.tutoringsystem.dto;

public class ReportMetric {

    private final String label;
    private final String value;

    public ReportMetric(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}
