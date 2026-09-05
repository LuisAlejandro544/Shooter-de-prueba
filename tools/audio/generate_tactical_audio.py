#!/usr/bin/env python3
"""
PolyStrike 3D - Audio Generator for Tactical Firearms
Generates high quality, realistic PCM audio files (WAV 16-bit 44.1kHz mono)
for gunfire blasts (suppressed, assault rifle, shotgun, sniper, pistol, smg)
and mechanical reload sequences (mag release, mag slide-out, mag insert, bolt rack).

License: 100% CC0 / MIT (Public Domain / Pure Mathematical Audio Synthesis).
No proprietary audio samples or attribution requirements.
"""

import math
import struct
import random
import os
import argparse

SAMPLE_RATE = 44100

def write_wav(filepath, samples):
    """Escribe una lista de floats [-1.0, 1.0] en un archivo WAV PCM 16-bit."""
    os.makedirs(os.path.dirname(os.path.abspath(filepath)), exist_ok=True)
    num_samples = len(samples)
    byte_rate = SAMPLE_RATE * 2
    block_align = 2
    data_size = num_samples * 2
    chunk_size = 36 + data_size

    with open(filepath, "wb") as f:
        # RIFF Header
        f.write(b"RIFF")
        f.write(struct.pack("<I", chunk_size))
        f.write(b"WAVE")
        # fmt subchunk
        f.write(b"fmt ")
        f.write(struct.pack("<I", 16))          # Subchunk1Size (16 for PCM)
        f.write(struct.pack("<H", 1))           # AudioFormat (1 = PCM)
        f.write(struct.pack("<H", 1))           # NumChannels (1 = Mono)
        f.write(struct.pack("<I", SAMPLE_RATE)) # SampleRate
        f.write(struct.pack("<I", byte_rate))   # ByteRate
        f.write(struct.pack("<H", block_align)) # BlockAlign
        f.write(struct.pack("<H", 16))          # BitsPerSample (16 bits)
        # data subchunk
        f.write(b"data")
        f.write(struct.pack("<I", data_size))

        for s in samples:
            clamped = max(-1.0, min(1.0, s))
            int_val = int(clamped * 32767.0)
            f.write(struct.pack("<h", int_val))
    print(f"[AUDIO] Generado exitosamente: {filepath} ({num_samples / SAMPLE_RATE:.2f}s)")


def generate_gunfire_audio(weapon_model, output_path):
    """
    Sintetiza audio realista de disparo balístico con onda de choque supersónica (crack inicial),
    explosión de propelente en recámara, cuerpo grave subsónico y cola de reverberación balística.
    """
    random.seed(42) # Reproducible

    # Parámetros acústicos por tipo de arma
    if "sniper" in weapon_model:
        duration = 0.85
        thump_freq = 70.0
        crack_volume = 1.0
        decay_time = 0.55
        metallic_ring = True
    elif "shotgun" in weapon_model:
        duration = 0.70
        thump_freq = 55.0
        crack_volume = 0.95
        decay_time = 0.45
        metallic_ring = False
    elif "smg" in weapon_model:
        duration = 0.35
        thump_freq = 110.0
        crack_volume = 0.8
        decay_time = 0.22
        metallic_ring = False
    elif "pistol" in weapon_model or "revolver" in weapon_model:
        duration = 0.40
        thump_freq = 95.0
        crack_volume = 0.85
        decay_time = 0.28
        metallic_ring = False
    elif "suppressed" in weapon_model or "covert" in weapon_model:
        duration = 0.25
        thump_freq = 160.0
        crack_volume = 0.35
        decay_time = 0.12
        metallic_ring = False
    else: # Fusil de asalto estándar / ametralladora
        duration = 0.50
        thump_freq = 82.0
        crack_volume = 0.9
        decay_time = 0.38
        metallic_ring = True

    num_samples = int(SAMPLE_RATE * duration)
    samples = [0.0] * num_samples

    # Filtro paso-bajo simple para suavizar el ruido blanco de la deflagración
    filter_val = 0.0
    alpha = 0.35

    for i in range(num_samples):
        t = i / SAMPLE_RATE

        # 1. Crack inicial (onda de choque balística en los primeros 15ms)
        crack = 0.0
        if t < 0.015:
            # Pulso no lineal saturado
            crack = (random.random() * 2.0 - 1.0) * (1.0 - t / 0.015) * crack_volume

        # 2. Deflagración de pólvora / explosión (ruido filtrado)
        noise = random.random() * 2.0 - 1.0
        filter_val += alpha * (noise - filter_val)
        explosion_env = math.exp(-t / (decay_time * 0.5))
        explosion = filter_val * explosion_env

        # 3. Golpe sordo subsónico (Thump de recámara)
        thump_env = math.exp(-t / (decay_time * 0.35))
        freq_decay = thump_freq * (1.0 + 3.0 * math.exp(-t * 80.0))
        thump = math.sin(2.0 * math.pi * freq_decay * t) * thump_env * 0.9

        # 4. Resonancia metálica del receptor / cañón
        ring = 0.0
        if metallic_ring and t > 0.01:
            ring_env = math.exp(-t / 0.25)
            ring = math.sin(2.0 * math.pi * 920.0 * t) * ring_env * 0.15

        sample = (crack * 0.6) + (explosion * 0.7) + (thump * 0.75) + ring
        samples[i] = sample

    # Normalización para evitar clipping
    max_amp = max(abs(s) for s in samples) if samples else 1.0
    if max_amp > 0:
        samples = [s / max_amp * 0.96 for s in samples]

    write_wav(output_path, samples)


def generate_reload_audio(weapon_model, output_path):
    """
    Sintetiza la secuencia mecánica de recarga táctica:
    1. Expulsión del cargador (click de retén + deslizamiento plástico/metálico).
    2. Inserción firme del nuevo cargador (doble click de bloqueo balístico).
    3. Liberación de cerrojo o cerrojazo (impacto de acero templado).
    """
    random.seed(99)
    duration = 1.6
    num_samples = int(SAMPLE_RATE * duration)
    samples = [0.0] * num_samples

    def add_metallic_click(start_time, duration_click=0.035, freq=1800.0, intensity=0.7):
        start_idx = int(start_time * SAMPLE_RATE)
        click_samples = int(duration_click * SAMPLE_RATE)
        for i in range(click_samples):
            idx = start_idx + i
            if idx >= num_samples:
                break
            t = i / SAMPLE_RATE
            env = math.exp(-t * 120.0)
            noise = (random.random() * 2.0 - 1.0) * 0.4
            tone = math.sin(2.0 * math.pi * freq * t) * 0.6
            samples[idx] += (noise + tone) * env * intensity

    def add_slide_friction(start_time, duration_slide=0.18, intensity=0.35):
        start_idx = int(start_time * SAMPLE_RATE)
        slide_samples = int(duration_slide * SAMPLE_RATE)
        f_val = 0.0
        for i in range(slide_samples):
            idx = start_idx + i
            if idx >= num_samples:
                break
            t = i / SAMPLE_RATE
            env = math.sin(math.pi * (t / duration_slide))
            noise = random.random() * 2.0 - 1.0
            f_val += 0.25 * (noise - f_val)
            samples[idx] += f_val * env * intensity

    # Secuencia de recarga:
    # 0.15s: Liberación de cargador gastado
    add_metallic_click(0.15, duration_click=0.04, freq=1400.0, intensity=0.75)
    add_slide_friction(0.18, duration_slide=0.16, intensity=0.4)

    # 0.85s: Inserción del nuevo cargador cargado
    add_slide_friction(0.82, duration_slide=0.12, intensity=0.5)
    add_metallic_click(0.92, duration_click=0.03, freq=1100.0, intensity=0.7)
    add_metallic_click(0.96, duration_click=0.045, freq=1600.0, intensity=0.95)

    # 1.30s: Cerrojo / Slide rack (Liberación de corredera o amartillado)
    add_metallic_click(1.28, duration_click=0.03, freq=2200.0, intensity=0.7)
    add_slide_friction(1.30, duration_slide=0.10, intensity=0.45)
    add_metallic_click(1.38, duration_click=0.06, freq=950.0, intensity=0.98)

    # Normalizar
    max_amp = max(abs(s) for s in samples) if samples else 1.0
    if max_amp > 0:
        samples = [s / max_amp * 0.92 for s in samples]

    write_wav(output_path, samples)


def main():
    parser = argparse.ArgumentParser(description="Generador de audio táctico militar (100% CC0)")
    parser.add_argument("--model", type=str, required=True, help="Identificador del arma")
    parser.add_argument("--output_dir", type=str, default="generated_assets/audio", help="Directorio destino")
    args = parser.parse_args()

    os.makedirs(args.output_dir, exist_ok=True)
    fire_path = os.path.join(args.output_dir, f"{args.model}_fire.wav")
    reload_path = os.path.join(args.output_dir, f"{args.model}_reload.wav")

    generate_gunfire_audio(args.model, fire_path)
    generate_reload_audio(args.model, reload_path)


if __name__ == "__main__":
    main()
