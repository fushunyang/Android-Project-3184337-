    // Giant Tree Mode unlocked at 8000 steps
    fun enterGiantTreeMode() {
        if (currentSteps >= 8000) {
            scaleFactor = 3.0f
            enableRealTimeShadow = true
            trunkWidthMultiplier = 3.0f
            enableParticleEffects = true
        }
    }
