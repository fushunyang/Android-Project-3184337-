package com.example.tree

import kotlin.random.Random

object LeafParticleSystem {
    val leaves = mutableListOf<Leaf>()

    fun updateAndSpawnLeaves() {
        if (TreeManager.currentSteps % 100 == 0L) {
            repeat(5) {
                leaves.add(Leaf(
                    x = Random.nextFloat() * 1080,
                    y = -100f,
                    velocityY = Random.nextFloat() * 3 + 2,
                    rotationSpeed = Random.nextFloat() * 10 - 5
                ))
            }
        }
        leaves.forEach { it.update() }
        leaves.removeIf { it.y > 1920 }
    }
}

data class Leaf(
    var x: Float,
    var y: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    var rotation: Float = 0f
) {
    fun update() {
        y += velocityY
        rotation += rotationSpeed
    }
}
