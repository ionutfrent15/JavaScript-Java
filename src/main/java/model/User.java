package model;

import java.io.Serializable;

public class User implements Serializable {

    private int id;
    private String username;
    private double secunde;


    public User(String username, double secunde) {
        this.username = username;
        this.secunde = secunde;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getSecunde() {
        return secunde;
    }

    public void setSecunde(double secunde) {
        this.secunde = secunde;
    }
}
