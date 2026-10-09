package org.annpath.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing Explainable Matching breakdown factors and summary.
 * Transforms black-box matching scores into transparent, human-readable decision factors.
 */
public class MatchExplanation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String factor;
    private int score;          // 0 - 100
    private double weight;      // e.g., 0.25
    private String explanation; // e.g., "1.4 km from destination within 15 km service radius"
    private boolean matched;    // true if requirement met or positive factor
    private String icon;        // SVG or UI icon identifier

    public MatchExplanation() {}

    public MatchExplanation(String factor, int score, double weight, String explanation, boolean matched, String icon) {
        this.factor = factor;
        this.score = Math.max(0, Math.min(100, score));
        this.weight = weight;
        this.explanation = explanation;
        this.matched = matched;
        this.icon = icon;
    }

    public String getFactor() { return factor; }
    public void setFactor(String factor) { this.factor = factor; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = Math.max(0, Math.min(100, score)); }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public boolean isMatched() { return matched; }
    public void setMatched(boolean matched) { this.matched = matched; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    /**
     * Composite explanation report containing aggregate scores and risk considerations.
     */
    public static class Summary implements Serializable {
        private static final long serialVersionUID = 1L;

        private double totalScore;
        private String headline;
        private String matchGrade; // EXCELLENT, HIGH, MODERATE, LOW
        private List<MatchExplanation> breakdown = new ArrayList<>();
        private List<String> positiveFactors = new ArrayList<>();
        private List<String> potentialRisks = new ArrayList<>();

        public Summary() {}

        public Summary(double totalScore, String headline) {
            this.totalScore = Math.round(totalScore * 10.0) / 10.0;
            this.headline = headline;
            if (this.totalScore >= 85) this.matchGrade = "EXCELLENT";
            else if (this.totalScore >= 70) this.matchGrade = "HIGH";
            else if (this.totalScore >= 50) this.matchGrade = "MODERATE";
            else this.matchGrade = "LOW";
        }

        public double getTotalScore() { return totalScore; }
        public void setTotalScore(double totalScore) {
            this.totalScore = Math.round(totalScore * 10.0) / 10.0;
            if (this.totalScore >= 85) this.matchGrade = "EXCELLENT";
            else if (this.totalScore >= 70) this.matchGrade = "HIGH";
            else if (this.totalScore >= 50) this.matchGrade = "MODERATE";
            else this.matchGrade = "LOW";
        }

        public String getHeadline() { return headline; }
        public void setHeadline(String headline) { this.headline = headline; }

        public String getMatchGrade() { return matchGrade; }
        public void setMatchGrade(String matchGrade) { this.matchGrade = matchGrade; }

        public List<MatchExplanation> getBreakdown() { return breakdown; }
        public void setBreakdown(List<MatchExplanation> breakdown) { this.breakdown = breakdown; }
        public void addFactor(MatchExplanation explanation) { this.breakdown.add(explanation); }

        public List<String> getPositiveFactors() { return positiveFactors; }
        public void setPositiveFactors(List<String> positiveFactors) { this.positiveFactors = positiveFactors; }
        public void addPositiveFactor(String factor) { this.positiveFactors.add(factor); }

        public List<String> getPotentialRisks() { return potentialRisks; }
        public void setPotentialRisks(List<String> potentialRisks) { this.potentialRisks = potentialRisks; }
        public void addPotentialRisk(String risk) { this.potentialRisks.add(risk); }
    }
}
