package com.example.payday;

import java.io.Serializable;

public class Operation implements Serializable {
    private String type;
    private String from;
    private int amount;
    private String date;
    private String referencedUserName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReferencedUserName() {
        return referencedUserName;
    }

    public void setReferencedUserName(String referencedUserName) {
        this.referencedUserName = referencedUserName;
    }
}
