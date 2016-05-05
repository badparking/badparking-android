package ua.in.badparking.model;

import ua.in.badparking.data.Report;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum ReportController {
    INST;

    private Report report = new Report();

    public Report getReport() {
        return report;
    }
}
