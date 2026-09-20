#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Генератор звуков для игры «МУРКА и КРЯ».

Ни одного чужого файла: всё считается математикой, поэтому лицензий нет.
Звуки нарочно короткие и мягкие — их услышат пятьдесят раз подряд.

Запуск:  python3 tools/make_sounds.py
Результат: app/src/main/res/raw/*.wav  (моно, 22050 Гц, 16 бит)

Чтобы поменять звук — правьте параметры ниже и перезапустите скрипт.
"""

import math
import os
import struct
import wave

RATE = 22050
OUT_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                       "app", "src", "main", "res", "raw")


def silence(duration):
    return [0.0] * int(RATE * duration)


def mix(base, addition, at=0.0):
    start = int(at * RATE)
    need = start + len(addition)
    if len(base) < need:
        base.extend([0.0] * (need - len(base)))
    for i, value in enumerate(addition):
        base[start + i] += value
    return base


def envelope(length, attack=0.006, release=0.6, curve=3.0):
    """Мягкая атака и экспоненциальный спад — без щелчков."""
    attack_samples = max(1, int(attack * RATE))
    out = []
    for i in range(length):
        if i < attack_samples:
            amplitude = i / attack_samples
        else:
            position = (i - attack_samples) / max(1, length - attack_samples)
            amplitude = math.exp(-curve * position) * (1.0 - position * (1.0 - release) * 0.0 + 0.0)
            amplitude *= (1.0 - position) ** 0.4
        out.append(amplitude)
    return out


def tone(freq_start, freq_end, duration, amplitude=0.6, harmonic=0.18, vibrato=0.0, curve=3.0):
    """Синус с плавным изменением высоты и лёгкой второй гармоникой — тёплый тембр."""
    length = int(duration * RATE)
    env = envelope(length, curve=curve)
    out = []
    phase = 0.0
    for i in range(length):
        position = i / max(1, length - 1)
        freq = freq_start + (freq_end - freq_start) * position
        if vibrato:
            freq *= 1.0 + vibrato * math.sin(2 * math.pi * 5.5 * i / RATE)
        phase += 2 * math.pi * freq / RATE
        value = math.sin(phase) + harmonic * math.sin(2 * phase)
        out.append(amplitude * env[i] * value / (1.0 + harmonic))
    return out


def noise(duration, amplitude=0.4, smooth=8, curve=4.0):
    """Шум, сглаженный скользящим средним, — получается «шшш», а не треск."""
    length = int(duration * RATE)
    env = envelope(length, curve=curve)
    seed = 12345
    raw = []
    for _ in range(length + smooth):
        seed = (1103515245 * seed + 12345) % (1 << 31)
        raw.append((seed / (1 << 30)) - 1.0)
    out = []
    for i in range(length):
        window = raw[i:i + smooth]
        out.append(amplitude * env[i] * sum(window) / smooth)
    return out


def soften(samples, window=3):
    """Простой фильтр верхних частот вниз: убирает резкость."""
    out = []
    for i in range(len(samples)):
        chunk = samples[max(0, i - window):i + 1]
        out.append(sum(chunk) / len(chunk))
    return out


def save(name, samples, peak=0.62):
    highest = max(1e-6, max(abs(value) for value in samples))
    scale = peak / highest
    path = os.path.join(OUT_DIR, name + ".wav")
    os.makedirs(OUT_DIR, exist_ok=True)
    with wave.open(path, "w") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(RATE)
        frames = b"".join(
            struct.pack("<h", int(max(-1.0, min(1.0, value * scale)) * 32000))
            for value in samples
        )
        handle.writeframes(frames)
    print("%-10s %5.2f c" % (name, len(samples) / RATE))


# --- Сами звуки -------------------------------------------------------------

def blup():
    """Касание клетки: короткий пузырёк."""
    return soften(tone(520, 880, 0.12, amplitude=0.55, harmonic=0.12, curve=5.0))


def place():
    """Фигура приземлилась: мягкий «туп» с щепоткой пыли."""
    body = tone(220, 140, 0.18, amplitude=0.7, harmonic=0.25, curve=4.5)
    dust = noise(0.09, amplitude=0.22, smooth=14, curve=6.0)
    return soften(mix(list(body), dust))


def win():
    """Победа: короткая весёлая фанфара до-ми-соль-до."""
    notes = [(523.25, 0.00), (659.25, 0.13), (783.99, 0.26), (1046.50, 0.39)]
    track = silence(1.15)
    for freq, at in notes:
        track = mix(track, tone(freq, freq, 0.34, amplitude=0.45, harmonic=0.3,
                                vibrato=0.004, curve=3.2), at=at)
    # Финальный аккорд подольше — чтобы фраза закончилась, а не оборвалась.
    for freq in (523.25, 659.25, 783.99):
        track = mix(track, tone(freq, freq, 0.45, amplitude=0.26, harmonic=0.2, curve=2.6), at=0.52)
    return soften(track)


def draw_sound():
    """Ничья: дружелюбное «пум-пум», без единой грустной ноты."""
    track = silence(0.75)
    track = mix(track, tone(392.00, 392.00, 0.26, amplitude=0.5, harmonic=0.25, curve=3.0), at=0.0)
    track = mix(track, tone(440.00, 440.00, 0.38, amplitude=0.5, harmonic=0.25, curve=2.6), at=0.22)
    track = mix(track, tone(587.33, 587.33, 0.30, amplitude=0.3, harmonic=0.2, curve=2.8), at=0.22)
    return soften(track)


def star():
    """Звёздочка прилетела в счёт: короткий взлёт."""
    body = tone(880, 1760, 0.22, amplitude=0.45, harmonic=0.22, curve=3.5)
    sparkle = tone(2093, 2093, 0.16, amplitude=0.18, harmonic=0.1, curve=4.0)
    return soften(mix(list(body), sparkle, at=0.08))


def erase():
    """Поле стирается ластиком."""
    return soften(noise(0.42, amplitude=0.5, smooth=26, curve=2.2), window=5)


# Громкость каждого звука подобрана на слух: касания тихие, потому что
# их будет пятьдесят подряд, победа — самая заметная.
SOUNDS = {
    "blup": (blup, 0.42),
    "place": (place, 0.50),
    "win": (win, 0.62),
    "draw": (draw_sound, 0.52),
    "star": (star, 0.46),
    "erase": (erase, 0.40),
}

if __name__ == "__main__":
    for name, (maker, peak) in SOUNDS.items():
        save(name, maker(), peak=peak)
    print("Готово:", OUT_DIR)
