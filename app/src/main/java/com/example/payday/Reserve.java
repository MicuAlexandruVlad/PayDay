package com.example.payday;

import java.io.Serializable;

public class Reserve implements Serializable {
    private String name;
    // short date to display
    private String sameDayReminderDate;
    // date with time used for reminder (ex: remind1 hour earlier)
    private String fullScheduledDate;
    private String referencedUsername;
    private String subtractionTime;
    private int goalAmount;
    private int actualAmount;
    private boolean reminderForScheduledDate;
    private boolean secondaryReminder;
    private boolean finalised;
    private String secondaryReminderDate;
    private String note;
    // 0 - low, 1- medium, 2 - high
    private int priority;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSameDayReminderDate() {
        return sameDayReminderDate;
    }

    public void setSameDayReminderDate(String sameDayReminderDate) {
        this.sameDayReminderDate = sameDayReminderDate;
    }

    public String getReferencedUsername() {
        return referencedUsername;
    }

    public void setReferencedUsername(String referencedUsername) {
        this.referencedUsername = referencedUsername;
    }

    public int getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(int goalAmount) {
        this.goalAmount = goalAmount;
    }

    public int getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(int actualAmount) {
        this.actualAmount = actualAmount;
    }

    public boolean hasReminderForScheduledDate() {
        return reminderForScheduledDate;
    }

    public void setReminderForScheduledDate(boolean reminderForScheduledDate) {
        this.reminderForScheduledDate = reminderForScheduledDate;
    }

    public boolean hasSecondaryReminder() {
        return secondaryReminder;
    }

    public void setSecondaryReminder(boolean secondaryReminder) {
        this.secondaryReminder = secondaryReminder;
    }

    public String getSecondaryReminderDate() {
        return secondaryReminderDate;
    }

    public void setSecondaryReminderDate(String secondaryReminderDate) {
        this.secondaryReminderDate = secondaryReminderDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getFullScheduledDate() {
        return fullScheduledDate;
    }

    public void setFullScheduledDate(String fullScheduledDate) {
        this.fullScheduledDate = fullScheduledDate;
    }

    public String getSubtractionTime() {
        return subtractionTime;
    }

    public void setSubtractionTime(String subtractionTime) {
        this.subtractionTime = subtractionTime;
    }

    public boolean isFinalised() {
        return finalised;
    }

    public void setFinalised(boolean finalised) {
        this.finalised = finalised;
    }
}
