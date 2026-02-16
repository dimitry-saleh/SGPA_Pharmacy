package com.pharmacy.sgpa.model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Commande {

    private int id;
    private Fournisseur fournisseur; // Represents the relationship
    private LocalDateTime dateCommande;
    private String statut; // "EN_ATTENTE" or "RECUE"

    // We reuse LignePanier to hold the list of drugs + quantities
    private List<LignePanier> lignes = new ArrayList<>();


    // This allows PropertyValueFactory("nomFournisseur") to work in the Table
    public String getNomFournisseur() {
        if (fournisseur != null) {
            return fournisseur.getNom();
        }
        return "Inconnu";
    }

    // Optional: Helper to format date for TableView
    public String getDateFormatee() {
        if (dateCommande != null) {
            return dateCommande.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<LignePanier> getLignes() {
        return lignes;
    }

    public void setLignes(List<LignePanier> lignes) {
        this.lignes = lignes;
    }
}