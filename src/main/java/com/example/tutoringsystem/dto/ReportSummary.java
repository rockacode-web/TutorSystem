package com.example.tutoringsystem.dto;

import java.util.List;

public class ReportSummary {

    private final String type;
    private final String title;
    private final String description;
    private final List<ReportMetric> metrics;

    public ReportSummary(String type, String title, String description, List<ReportMetric> metrics) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.metrics = metrics;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<ReportMetric> getMetrics() {
        return metrics;
    }
}
