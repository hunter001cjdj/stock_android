package com.example.twstockanalyzer.domain.model

enum class RiskRewardQuadrant(val label: String) {
    ALL("All"),
    HIGH_RISK_HIGH_REWARD("High Risk High Reward"),
    LOW_RISK_HIGH_REWARD("Low Risk High Reward"),
    HIGH_RISK_LOW_REWARD("High Risk Low Reward"),
    LOW_RISK_LOW_REWARD("Low Risk Low Reward")
}
