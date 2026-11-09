import math
import struct
import wave
import os
import subprocess

SAMPLE_RATE = 44100

def create_wav(filename, samples, sample_rate=SAMPLE_RATE):
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1) # mono
        wav_file.setsampwidth(2) # 16-bit
        wav_file.setframerate(sample_rate)
        # Clamp and pack samples
        packed = bytearray()
        for s in samples:
            val = int(max(-32767, min(32767, s * 32767.0)))
            packed.extend(struct.pack('<h', val))
        wav_file.writeframes(packed)

def encode_to_ogg(wav_path, ogg_path):
    subprocess.run([
        'ffmpeg', '-y', '-i', wav_path,
        '-ac', '2',
        '-c:a', 'vorbis', '-strict', '-2',
        '-b:a', '96k',
        ogg_path
    ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    os.remove(wav_path)

def generate_sfx():
    raw_dir = "app/src/main/res/raw"
    os.makedirs(raw_dir, exist_ok=True)
    temp_dir = "build/tmp_audio"
    os.makedirs(temp_dir, exist_ok=True)

    # 1. sfx_tap_pop: Cute wooden bubble pop (70ms)
    duration = 0.075
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 55.0)
        freq = 650.0 - (t / duration) * 350.0
        val = math.sin(2 * math.pi * freq * t) * env * 0.85
        samples.append(val)
    wav = f"{temp_dir}/sfx_tap_pop.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_tap_pop.ogg")

    # 2. sfx_dash_swoosh: Fast airy rushing dash (130ms)
    duration = 0.13
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    import random
    random.seed(42)
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.sin(math.pi * (t / duration)) ** 1.8
        freq = 300.0 + math.sin(t * 30.0) * 80.0
        noise = (random.random() * 2.0 - 1.0) * 0.4
        tone = math.sin(2 * math.pi * freq * t) * 0.6
        samples.append((tone + noise) * env * 0.8)
    wav = f"{temp_dir}/sfx_dash_swoosh.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_dash_swoosh.ogg")

    # 3. sfx_bonk_boing: Cartoon spring boing (240ms)
    duration = 0.24
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 12.0)
        vibrato = math.sin(2 * math.pi * 32.0 * t) * 45.0
        freq = 200.0 + vibrato - (t * 80.0)
        val = math.sin(2 * math.pi * freq * t) * env * 0.9
        samples.append(val)
    wav = f"{temp_dir}/sfx_bonk_boing.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_bonk_boing.ogg")

    # 4. sfx_escape_chime: Bright celestial bell chime (450ms)
    duration = 0.45
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 6.5)
        # Bell harmonics
        f0 = 880.0
        h1 = math.sin(2 * math.pi * f0 * t) * 0.6
        h2 = math.sin(2 * math.pi * f0 * 2.0 * t) * 0.3
        h3 = math.sin(2 * math.pi * f0 * 3.0 * t) * 0.15
        samples.append((h1 + h2 + h3) * env * 0.85)
    wav = f"{temp_dir}/sfx_escape_chime.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_escape_chime.ogg")

    # 5. sfx_mud_squelch: Low squishy puddle squelch (180ms)
    duration = 0.18
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 18.0)
        freq = 180.0 - (t / duration) * 110.0
        val = math.sin(2 * math.pi * freq * t + math.sin(2 * math.pi * 45 * t)) * env * 0.85
        samples.append(val)
    wav = f"{temp_dir}/sfx_mud_squelch.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_mud_squelch.ogg")

    # 6. sfx_turntable_click: Crisp mechanical wooden ratchet click (85ms)
    duration = 0.085
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 65.0)
        freq = 1200.0 - (t * 8000.0)
        val = math.sin(2 * math.pi * freq * t) * env * 0.8
        samples.append(val)
    wav = f"{temp_dir}/sfx_turntable_click.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_turntable_click.ogg")

    # 7. sfx_portal_whoosh: Shimmering ethereal portal sweep (320ms)
    duration = 0.32
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.sin(math.pi * (t / duration)) ** 1.5
        freq = 500.0 + math.sin(2 * math.pi * 14.0 * t) * 220.0
        val = math.sin(2 * math.pi * freq * t) * env * 0.85
        samples.append(val)
    wav = f"{temp_dir}/sfx_portal_whoosh.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_portal_whoosh.ogg")

    # 8. sfx_cage_unlock: Key click and lock release (250ms)
    duration = 0.25
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 14.0)
        val1 = math.sin(2 * math.pi * 1450.0 * t) * 0.5 if t < 0.05 else 0.0
        val2 = math.sin(2 * math.pi * 920.0 * (t - 0.04)) * 0.7 if t >= 0.04 else 0.0
        samples.append((val1 + val2) * env * 0.85)
    wav = f"{temp_dir}/sfx_cage_unlock.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_cage_unlock.ogg")

    # 9. sfx_yawn: Sweet gentle sleepy yawn (360ms)
    duration = 0.36
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.sin(math.pi * (t / duration)) ** 1.3
        freq = 380.0 - (t / duration) * 140.0
        val = (math.sin(2 * math.pi * freq * t) + 0.3 * math.sin(4 * math.pi * freq * t)) * env * 0.75
        samples.append(val)
    wav = f"{temp_dir}/sfx_yawn.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_yawn.ogg")

    # 10. sfx_star_pop: Crystal glockenspiel ping (220ms)
    duration = 0.22
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 15.0)
        f0 = 1567.98 # G6
        val = (math.sin(2 * math.pi * f0 * t) * 0.7 + math.sin(4 * math.pi * f0 * t) * 0.3) * env * 0.85
        samples.append(val)
    wav = f"{temp_dir}/sfx_star_pop.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_star_pop.ogg")

    # 11. sfx_berry_jingle: Rising 3-tone berry pickup arpeggio (240ms)
    duration = 0.24
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    notes = [1046.50, 1318.51, 1567.98] # C6, E6, G6
    seg = duration / 3.0
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        note_idx = min(2, int(t / seg))
        t_note = t - note_idx * seg
        env = math.exp(-t_note * 18.0)
        freq = notes[note_idx]
        val = math.sin(2 * math.pi * freq * t) * env * 0.8
        samples.append(val)
    wav = f"{temp_dir}/sfx_berry_jingle.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_berry_jingle.ogg")

    # 12. sfx_button_tap: Crisp tactile UI button tick (45ms)
    duration = 0.045
    n_samples = int(duration * SAMPLE_RATE)
    samples = []
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 80.0)
        freq = 900.0 - (t / duration) * 450.0
        val = math.sin(2 * math.pi * freq * t) * env * 0.8
        samples.append(val)
    wav = f"{temp_dir}/sfx_button_tap.wav"
    create_wav(wav, samples)
    encode_to_ogg(wav, f"{raw_dir}/sfx_button_tap.ogg")

def generate_music():
    raw_dir = "app/src/main/res/raw"
    temp_dir = "build/tmp_audio"
    
    # Generate 9 distinct soothing, gentle ambient musical loops (~4 seconds seamless loop each)
    # Uses pure mathematical pentatonic chords and gentle kalimba/harp timbres
    tracks = {
        "music_menu": [261.63, 329.63, 392.00, 523.25], # C major
        "music_world_1": [261.63, 293.66, 329.63, 392.00, 440.00], # Sunny Meadow (Pastoral)
        "music_world_2": [220.00, 261.63, 293.66, 329.63, 392.00], # Happy Farm (Rustic)
        "music_world_3": [196.00, 261.63, 293.66, 349.23, 392.00], # Jungle Ruins (Mystic)
        "music_world_4": [293.66, 369.99, 440.00, 587.33], # Snowy Peaks (Crystal)
        "music_world_5": [261.63, 329.63, 392.00, 493.88], # Coral Coast (Tropical)
        "music_world_6": [220.00, 277.18, 329.63, 440.00], # Moonlit Forest (Lullaby)
        "music_world_7": [246.94, 293.66, 369.99, 440.00], # Desert Oasis (Dune)
        "music_world_8": [261.63, 329.63, 392.00, 523.25, 659.25] # Sky Islands (Festival)
    }

    loop_duration = 4.0
    n_samples = int(loop_duration * SAMPLE_RATE)

    for name, scale in tracks.items():
        samples = [0.0] * n_samples
        step_len = loop_duration / len(scale)
        for idx, freq in enumerate(scale):
            start_t = idx * step_len
            for i in range(n_samples):
                t = i / SAMPLE_RATE
                rel_t = (t - start_t) % loop_duration
                if rel_t >= 0 and rel_t < 1.2:
                    env = math.exp(-rel_t * 3.5)
                    tone = (math.sin(2 * math.pi * freq * t) * 0.7 +
                            math.sin(2 * math.pi * freq * 2.0 * t) * 0.25) * env
                    samples[i] += tone * 0.35

        # Seamless looping crossfade at edges
        fade_len = int(0.1 * SAMPLE_RATE)
        for i in range(fade_len):
            gain = i / float(fade_len)
            samples[i] = samples[i] * gain + samples[n_samples - fade_len + i] * (1.0 - gain)
            samples[n_samples - fade_len + i] = samples[i]

        wav = f"{temp_dir}/{name}.wav"
        create_wav(wav, samples)
        encode_to_ogg(wav, f"{raw_dir}/{name}.ogg")

if __name__ == "__main__":
    generate_sfx()
    generate_music()
    print("Successfully generated all CC0 SFX and music loops!")
