package com.pharmacy.sgpa.model;



public class Fournisseur {
    private int id;
    private String nom;
    private String contact; // Phone number
    private String email;
    private String adresse;

    // Constructor without ID (for creation)
    public Fournisseur(String nom, String contact, String email, String adresse) {
        this.nom = nom;
        this.contact = contact;
        this.email = email;
        this.adresse = adresse;
    }

    public Fournisseur() {
    }

    public Fournisseur(int id, String nom, String contact, String email, String adresse) {
        this.id = id;
        this.nom = nom;
        this.contact = contact;
        this.email = email;
        this.adresse = adresse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}