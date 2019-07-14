package model;

import java.io.Serializable;

public class Imagine implements Serializable {
    private String nume;
    private String descriere;

    public Imagine(String nume, String descriere) {
        this.nume = nume;
        this.descriere = descriere;
    }

    public Imagine() {
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }
}
