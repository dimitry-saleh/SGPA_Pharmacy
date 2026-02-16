package com.pharmacy.sgpa.service;

import com.pharmacy.sgpa.dao.StatsDAO;
import com.pharmacy.sgpa.model.Medicament;

public class PredictionService {

    private StatsDAO statsDAO = new StatsDAO();

    // The Magic Algorithm
    public int calculateSuggestedOrder(Medicament m) {
        // 1. Analyze last 30 days
        int soldLastMonth = statsDAO.getSalesVolume(m.getId(), 30);

        // 2. Calculate Daily Average
        double dailyAvg = soldLastMonth / 30.0;

        // 3. Predict need for next 7 days (Safety buffer)
        int predictedNeed = (int) Math.ceil(dailyAvg * 7);

        // 4. Determine Gap
        int gap = predictedNeed - m.getStockActuel();

        // Only suggest ordering if we don't have enough
        return (gap > 0) ? gap : 0;
    }
}