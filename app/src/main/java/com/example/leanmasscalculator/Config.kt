package com.example.leanmasscalculator

object Config {
    const val MALE_THRESHOLD = 38.0
    const val FEMALE_THRESHOLD = 24.0

    fun calculateLBM(gender: String, weight: Double, height: Double): Double {
        return if (gender == "Homme") {
            (0.407 * weight) + (0.267 * height) - 19.2
        } else {
            (0.252 * weight) + (0.473 * height) - 48.3
        }
    }

    fun isSatisfactory(gender: String, lbm: Double): Boolean {
        return if (gender == "Homme") {
            lbm >= MALE_THRESHOLD
        } else {
            lbm >= FEMALE_THRESHOLD
        }
    }
}