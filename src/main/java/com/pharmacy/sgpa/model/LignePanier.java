package com.pharmacy.sgpa.model;



public class LignePanier {

    private Medicament medicament;
    private int quantite;
    private double sousTotal;

    // Constructor
    public LignePanier(Medicament m, int q) {
        this.medicament = m;
        this.quantite = q;
        this.sousTotal = m.getPrixPublic() * q;
    }

    // This is crucial for the TableView to show the name
    // The PropertyValueFactory("nomMedicament") looks for this specific method
    public String getNomMedicament() {
        return medicament.getNomCommercial();
    }

    public Medicament getMedicament() {
        return medicament;
    }

    public void setMedicament(Medicament medicament) {
        this.medicament = medicament;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getSousTotal() {
        return sousTotal;
    }

    public void setSousTotal(double sousTotal) {
        this.sousTotal = sousTotal;
    }
}