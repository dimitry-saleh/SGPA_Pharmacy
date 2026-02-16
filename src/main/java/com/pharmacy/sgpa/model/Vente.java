package com.pharmacy.sgpa.model;

import java.time.LocalDateTime;
import java.util.List;


public class Vente {
    private int id;
    private LocalDateTime dateVente;
    private double montantTotal;

    private boolean surOrdonnance;

    private List<LignePanier> lignes; // The items in this sale

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public boolean isSurOrdonnance() {
        return surOrdonnance;
    }

    public void setSurOrdonnance(boolean surOrdonnance) {
        this.surOrdonnance = surOrdonnance;
    }

    public List<LignePanier> getLignes() {
        return lignes;
    }

    public void setLignes(List<LignePanier> lignes) {
        this.lignes = lignes;
    }
}