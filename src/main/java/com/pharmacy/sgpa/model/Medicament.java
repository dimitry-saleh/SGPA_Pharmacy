package com.pharmacy.sgpa.model;


import java.time.LocalDate;


public class Medicament {

    private int id;
    private String nomCommercial;
    private String principeActif;
    private String formeGalenique;
    private String dosage;

    // Financials
    private double prixAchat;
    private double prixPublic;

    // Inventory
    private int stockActuel;
    private int seuilMin;

    private LocalDate datePeremption;
    private boolean necessiteOrdonnance;
    private int fournisseurId;

    public Medicament() {
    }

    public Medicament(int id, String nomCommercial, String principeActif, String formeGalenique, String dosage, double prixAchat, double prixPublic, int stockActuel, int seuilMin, LocalDate datePeremption, boolean necessiteOrdonnance, int fournisseurId) {
        this.id = id;
        this.nomCommercial = nomCommercial;
        this.principeActif = principeActif;
        this.formeGalenique = formeGalenique;
        this.dosage = dosage;
        this.prixAchat = prixAchat;
        this.prixPublic = prixPublic;
        this.stockActuel = stockActuel;
        this.seuilMin = seuilMin;
        this.datePeremption = datePeremption;
        this.necessiteOrdonnance = necessiteOrdonnance;
        this.fournisseurId = fournisseurId;
    }

    // A helper method to check if stock is low
    public boolean isStockLow() {
        return stockActuel <= seuilMin;
    }

    // A helper method to calculate Margin
    public double getMarge() {
        return prixPublic - prixAchat;
    }
    public int getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(int fournisseurId) { this.fournisseurId = fournisseurId; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
    }

    public String getPrincipeActif() {
        return principeActif;
    }

    public void setPrincipeActif(String principeActif) {
        this.principeActif = principeActif;
    }

    public String getFormeGalenique() {
        return formeGalenique;
    }

    public void setFormeGalenique(String formeGalenique) {
        this.formeGalenique = formeGalenique;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public double getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(double prixAchat) {
        this.prixAchat = prixAchat;
    }

    public double getPrixPublic() {
        return prixPublic;
    }

    public void setPrixPublic(double prixPublic) {
        this.prixPublic = prixPublic;
    }

    public int getStockActuel() {
        return stockActuel;
    }

    public void setStockActuel(int stockActuel) {
        this.stockActuel = stockActuel;
    }

    public int getSeuilMin() {
        return seuilMin;
    }

    public void setSeuilMin(int seuilMin) {
        this.seuilMin = seuilMin;
    }

    public LocalDate getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(LocalDate datePeremption) {
        this.datePeremption = datePeremption;
    }

    public boolean isNecessiteOrdonnance() {
        return necessiteOrdonnance;
    }

    public void setNecessiteOrdonnance(boolean necessiteOrdonnance) {
        this.necessiteOrdonnance = necessiteOrdonnance;
    }



}