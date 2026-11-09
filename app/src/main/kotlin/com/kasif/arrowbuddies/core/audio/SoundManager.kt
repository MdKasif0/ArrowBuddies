package com.kasif.arrowbuddies.core.audio

interface SoundManager {
    fun playPop()
    fun playBoing()
    fun playChime()
    fun playMusic(worldId: Int)
    fun stopMusic()
    fun setSoundEnabled(enabled: Boolean)
    fun setMusicEnabled(enabled: Boolean)
}

class DefaultSoundManager : SoundManager {
    private var soundEnabled = true
    private var musicEnabled = true

    override fun playPop() {
        // Implementation in Step 8
    }

    override fun playBoing() {
        // Implementation in Step 8
    }

    override fun playChime() {
        // Implementation in Step 8
    }

    override fun playMusic(worldId: Int) {
        // Implementation in Step 8
    }

    override fun stopMusic() {
        // Implementation in Step 8
    }

    override fun setSoundEnabled(enabled: Boolean) {
        this.soundEnabled = enabled
    }

    override fun setMusicEnabled(enabled: Boolean) {
        this.musicEnabled = enabled
    }
}

