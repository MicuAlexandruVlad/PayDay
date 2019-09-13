package com.example.payday;

import java.io.Serializable;

public class DBLinks implements Serializable {
    private String baseLink = "https://6996c1b2.ngrok.io/";

    public String getBaseLink() {
        return baseLink;
    }

    public void setBaseLink(String baseLink) {
        this.baseLink = baseLink;
    }
}
