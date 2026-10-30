package com.vaishnavi.sms.dto;

/**
 * Calculated attendance figures for one student. Rules: 42 total classes, 75% minimum,
 * so at least 32 classes must be attended.
 */
public class AttendanceSummary {

    public static final int TOTAL_CLASSES = 42;
    public static final double MIN_PERCENTAGE = 75.0;
    public static final int MIN_CLASSES_REQUIRED = (int) Math.ceil(TOTAL_CLASSES * MIN_PERCENTAGE / 100.0);

    private final int totalClasses = TOTAL_CLASSES;
    private final int minimumClassesRequired = MIN_CLASSES_REQUIRED;
    private final int classesAttended;
    private final int classesMissed;
    private final double percentage;
    private final boolean eligible;
    private final String status;
    private final int additionalClassesRequired;
    private final String message;

    private AttendanceSummary(int attended) {
        this.classesAttended = attended;
        this.classesMissed = TOTAL_CLASSES - attended;
        this.percentage = calculatePercentage(attended);
        this.eligible = attended >= MIN_CLASSES_REQUIRED;
        this.status = eligible ? "ELIGIBLE" : "SHORTAGE";
        this.additionalClassesRequired = eligible ? 0 : MIN_CLASSES_REQUIRED - attended;
        this.message = eligible
                ? "You meet the minimum attendance requirement of 75%."
                : "Attendance shortage: you must attend " + additionalClassesRequired
                + " more class" + (additionalClassesRequired == 1 ? "" : "es") + " to reach 75%.";
    }

    public static AttendanceSummary of(int classesAttended) {
        return new AttendanceSummary(classesAttended);
    }

    /** (attended / 42) x 100, rounded to two decimals. */
    public static double calculatePercentage(int attended) {
        return Math.round(attended * 10000.0 / TOTAL_CLASSES) / 100.0;
    }

    public int getTotalClasses() { return totalClasses; }
    public int getMinimumClassesRequired() { return minimumClassesRequired; }
    public int getClassesAttended() { return classesAttended; }
    public int getClassesMissed() { return classesMissed; }
    public double getPercentage() { return percentage; }
    public boolean isEligible() { return eligible; }
    public String getStatus() { return status; }
    public int getAdditionalClassesRequired() { return additionalClassesRequired; }
    public String getMessage() { return message; }
}
