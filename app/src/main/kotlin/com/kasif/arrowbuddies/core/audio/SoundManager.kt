package com.kasif.arrowbuddies.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kasif.arrowbuddies.R

interface SoundManager {
    fun playPop()
    fun playBoing()
    fun playChime()
    fun playMusic(worldId: Int)
    fun stopMusic()
    fun setSoundEnabled(enabled: Boolean)
    fun setMusicEnabled(enabled: Boolean)

    // Step 10 SFX hooks
    fun playTap() { playPop() }
    fun playDash() {}
    fun playBonk() { playBoing() }
    fun playEscape(rescueIndex: Int = 0) { playChime() }
    fun playMudSquelch() {}
    fun playTurntableClick() {}
    fun playPortalWhoosh() {}
    fun playCageUnlock() {}
    fun playYawn() {}
    fun playStarPop() {}
    fun playBerryJingle() {}
    fun playButtonTap() { playPop() }

    // Navigation / lifecycle hooks
    fun playMenuMusic() { playMusic(0) }
    fun pauseAll() {}
    fun resumeAll() {}
    fun release() {}
}

class DefaultSoundManager(
    private val context: Context? = null
) : SoundManager {

    private val tag = "SoundManager"
    private var soundEnabled = true
    private var musicEnabled = true
    private var isAppPaused = false

    private val handler = Handler(Looper.getMainLooper())
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // SoundPool for low-latency SFX
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>() // resId -> soundId

    // MediaPlayer for background music crossfading
    private var currentMusicPlayer: MediaPlayer? = null
    private var fadingMusicPlayer: MediaPlayer? = null
    private var currentWorldId: Int? = null
    private var musicBaseVolume = 1.0f
    private var isDuckedForSfx = false

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pauseMusicInternal()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pauseMusicInternal()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                setMusicVolumeInternal(0.25f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                setMusicVolumeInternal(1.0f)
                if (musicEnabled && !isAppPaused) {
                    resumeMusicInternal()
                }
            }
        }
    }

    init {
        context?.let { ctx ->
            audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            initSoundPool(ctx)
        }
    }

    private fun initSoundPool(ctx: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        val sfxResources = listOf(
            R.raw.sfx_tap_pop,
            R.raw.sfx_dash_swoosh,
            R.raw.sfx_bonk_boing,
            R.raw.sfx_escape_chime,
            R.raw.sfx_mud_squelch,
            R.raw.sfx_turntable_click,
            R.raw.sfx_portal_whoosh,
            R.raw.sfx_cage_unlock,
            R.raw.sfx_yawn,
            R.raw.sfx_star_pop,
            R.raw.sfx_berry_jingle,
            R.raw.sfx_button_tap
        )

        for (resId in sfxResources) {
            try {
                soundPool?.let { sp ->
                    soundMap[resId] = sp.load(ctx, resId, 1)
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to preload sound resource: $resId", e)
            }
        }
    }

    private fun playSfx(resId: Int, pitch: Float = 1.0f, volume: Float = 1.0f) {
        if (!soundEnabled || isAppPaused) return
        val soundId = soundMap[resId] ?: return
        soundPool?.play(soundId, volume, volume, 1, 0, pitch.coerceIn(0.5f, 2.0f))
        duckMusicBriefly()
    }

    private val unduckRunnable = Runnable {
        isDuckedForSfx = false
        if (musicEnabled && !isAppPaused) {
            setMusicVolumeInternal(musicBaseVolume)
        }
    }

    private fun duckMusicBriefly() {
        if (!musicEnabled || isAppPaused || currentMusicPlayer == null) return
        isDuckedForSfx = true
        setMusicVolumeInternal(musicBaseVolume * 0.4f)
        handler.removeCallbacks(unduckRunnable)
        handler.postDelayed(unduckRunnable, 350L)
    }

    override fun playPop() = playTap()
    override fun playBoing() = playBonk()
    override fun playChime() = playEscape(0)

    override fun playTap() = playSfx(R.raw.sfx_tap_pop)
    override fun playDash() = playSfx(R.raw.sfx_dash_swoosh)
    override fun playBonk() = playSfx(R.raw.sfx_bonk_boing)

    override fun playEscape(rescueIndex: Int) {
        val pitch = (1.0f + 0.05f * rescueIndex).coerceIn(1.0f, 1.8f)
        playSfx(R.raw.sfx_escape_chime, pitch = pitch)
    }

    override fun playMudSquelch() = playSfx(R.raw.sfx_mud_squelch)
    override fun playTurntableClick() = playSfx(R.raw.sfx_turntable_click)
    override fun playPortalWhoosh() = playSfx(R.raw.sfx_portal_whoosh)
    override fun playCageUnlock() = playSfx(R.raw.sfx_cage_unlock)
    override fun playYawn() = playSfx(R.raw.sfx_yawn)
    override fun playStarPop() = playSfx(R.raw.sfx_star_pop)
    override fun playBerryJingle() = playSfx(R.raw.sfx_berry_jingle)
    override fun playButtonTap() = playSfx(R.raw.sfx_button_tap)

    override fun playMusic(worldId: Int) {
        if (currentWorldId == worldId && (currentMusicPlayer?.isPlaying == true || !musicEnabled)) {
            return
        }
        currentWorldId = worldId
        if (!musicEnabled || isAppPaused || context == null) return

        val musicResId = when (worldId) {
            1 -> R.raw.music_world_1
            2 -> R.raw.music_world_2
            3 -> R.raw.music_world_3
            4 -> R.raw.music_world_4
            5 -> R.raw.music_world_5
            6 -> R.raw.music_world_6
            7 -> R.raw.music_world_7
            8 -> R.raw.music_world_8
            else -> R.raw.music_menu
        }

        requestAudioFocus()
        crossfadeToMusic(musicResId)
    }

    private fun crossfadeToMusic(resId: Int) {
        val ctx = context ?: return
        try {
            val newPlayer = MediaPlayer.create(ctx, resId) ?: return
            newPlayer.isLooping = true
            newPlayer.setVolume(0f, 0f)
            newPlayer.start()

            val oldPlayer = currentMusicPlayer
            currentMusicPlayer = newPlayer
            fadingMusicPlayer = oldPlayer

            // Crossfade over 500 ms
            val durationMs = 500L
            val steps = 10
            val interval = durationMs / steps

            for (i in 1..steps) {
                handler.postDelayed({
                    val fraction = i.toFloat() / steps
                    val targetVol = if (isDuckedForSfx) musicBaseVolume * 0.4f else musicBaseVolume
                    newPlayer.setVolume(targetVol * fraction, targetVol * fraction)
                    oldPlayer?.setVolume(targetVol * (1f - fraction), targetVol * (1f - fraction))

                    if (i == steps) {
                        try {
                            oldPlayer?.stop()
                            oldPlayer?.release()
                        } catch (ignored: Exception) {}
                        if (fadingMusicPlayer == oldPlayer) fadingMusicPlayer = null
                    }
                }, i * interval)
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to start music player for res: $resId", e)
        }
    }

    override fun stopMusic() {
        currentWorldId = null
        abandonAudioFocus()
        try {
            currentMusicPlayer?.stop()
            currentMusicPlayer?.release()
            currentMusicPlayer = null

            fadingMusicPlayer?.stop()
            fadingMusicPlayer?.release()
            fadingMusicPlayer = null
        } catch (ignored: Exception) {}
    }

    private fun pauseMusicInternal() {
        try {
            if (currentMusicPlayer?.isPlaying == true) {
                currentMusicPlayer?.pause()
            }
        } catch (ignored: Exception) {}
    }

    private fun resumeMusicInternal() {
        if (!musicEnabled || isAppPaused) return
        try {
            if (currentMusicPlayer != null && currentMusicPlayer?.isPlaying == false) {
                requestAudioFocus()
                currentMusicPlayer?.start()
                setMusicVolumeInternal(musicBaseVolume)
            } else if (currentMusicPlayer == null && currentWorldId != null) {
                playMusic(currentWorldId ?: 0)
            }
        } catch (ignored: Exception) {}
    }

    private fun setMusicVolumeInternal(volume: Float) {
        val vol = volume.coerceIn(0f, 1f)
        try {
            currentMusicPlayer?.setVolume(vol, vol)
        } catch (ignored: Exception) {}
    }

    override fun setSoundEnabled(enabled: Boolean) {
        this.soundEnabled = enabled
    }

    override fun setMusicEnabled(enabled: Boolean) {
        this.musicEnabled = enabled
        if (enabled) {
            if (!isAppPaused) {
                currentWorldId?.let { playMusic(it) } ?: playMenuMusic()
            }
        } else {
            pauseMusicInternal()
        }
    }

    override fun pauseAll() {
        isAppPaused = true
        pauseMusicInternal()
        soundPool?.autoPause()
    }

    override fun resumeAll() {
        isAppPaused = false
        soundPool?.autoResume()
        if (musicEnabled) {
            resumeMusicInternal()
        }
    }

    override fun release() {
        abandonAudioFocus()
        stopMusic()
        try {
            soundPool?.release()
            soundPool = null
        } catch (ignored: Exception) {}
        handler.removeCallbacksAndMessages(null)
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attr)
                .setOnAudioFocusChangeListener(audioFocusChangeListener, handler)
                .build()
            audioFocusRequest = req
            am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(audioFocusChangeListener)
        }
    }
}
