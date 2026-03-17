package com.example.twstockanalyzer.domain.model

enum class RiskRewardQuadrant(val label: String) {
    ALL("全部"),
    HIGH_RISK_HIGH_REWARD("高風險高報酬"),
    LOW_RISK_HIGH_REWARD("低風險高報酬"),
    HIGH_RISK_LOW_REWARD("高風險低報酬"),
    LOW_RISK_LOW_REWARD("低風險低報酬")
}
