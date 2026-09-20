#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Ищет на лицах зрачки и цвет кожи, чтобы игра умела рисовать закрытые веки.

Печатает координаты в долях от стороны картинки — их вставляют в ui/Cast.kt.
Заодно кладёт рядом превью с «закрытыми глазами»: видно, попали веки или нет.

    python3 tools/find_eyes.py
"""

import os
from collections import deque

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FACES_DIR = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")
PREVIEW_DIR = os.path.join(ROOT, "art", "preview")

FACES = ["face_kira", "face_mama", "face_papa"]

DARK = 78          # темнее этого — кандидат в зрачок
MIN_AREA = 60      # мелкие пятна (ресницы, брови) отбрасываем
MAX_SIDE = 0.16    # пятно шире этого — уже не глаз, а волосы
ZONE_X = (0.24, 0.76)   # глаза ищем только в середине лица
ZONE_Y = (0.34, 0.70)


def luminance(pixel):
    return 0.299 * pixel[0] + 0.587 * pixel[1] + 0.114 * pixel[2]


def dark_blobs(image):
    width, height = image.size
    pixels = image.load()
    seen = bytearray(width * height)
    blobs = []
    top, bottom = int(height * ZONE_Y[0]), int(height * ZONE_Y[1])
    left_edge, right_edge = int(width * ZONE_X[0]), int(width * ZONE_X[1])

    for y in range(top, bottom):
        for x in range(left_edge, right_edge):
            index = y * width + x
            if seen[index]:
                continue
            pixel = pixels[x, y]
            if pixel[3] < 200 or luminance(pixel) > DARK:
                seen[index] = 1
                continue
            queue = deque([(x, y)])
            seen[index] = 1
            cells = []
            while queue:
                cx, cy = queue.popleft()
                cells.append((cx, cy))
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = cx + dx, cy + dy
                    if top <= ny < bottom and left_edge <= nx < right_edge:
                        ni = ny * width + nx
                        if not seen[ni]:
                            npix = pixels[nx, ny]
                            if npix[3] >= 200 and luminance(npix) <= DARK:
                                seen[ni] = 1
                                queue.append((nx, ny))
            if len(cells) >= MIN_AREA:
                xs = [c[0] for c in cells]
                ys = [c[1] for c in cells]
                if (max(xs) - min(xs)) > width * MAX_SIDE or (max(ys) - min(ys)) > height * MAX_SIDE:
                    continue
                blobs.append({
                    "area": len(cells),
                    "x0": min(xs), "x1": max(xs),
                    "y0": min(ys), "y1": max(ys),
                })
    return blobs


def skin_color(image, eye):
    """Цвет кожи берём из-под глаза — там щека, без румянца и волос."""
    pixels = image.load()
    cx = (eye["x0"] + eye["x1"]) // 2
    cy = eye["y1"] + (eye["y1"] - eye["y0"])
    samples = []
    for dx in range(-6, 7):
        for dy in range(-4, 5):
            x, y = cx + dx, cy + dy
            if 0 <= x < image.size[0] and 0 <= y < image.size[1]:
                pixel = pixels[x, y]
                if pixel[3] > 200:
                    samples.append(pixel[:3])
    if not samples:
        return (255, 214, 176)
    samples.sort(key=luminance)
    return samples[len(samples) // 2]


def analyse(name):
    path = os.path.join(FACES_DIR, name + ".png")
    image = Image.open(path).convert("RGBA")
    width, height = image.size
    blobs = dark_blobs(image)
    blobs.sort(key=lambda b: -b["area"])
    # Глаза — пара похожих пятен на одной высоте, симметричных относительно центра.
    pair = None
    for i in range(len(blobs)):
        for j in range(i + 1, len(blobs)):
            a, b = blobs[i], blobs[j]
            ay = (a["y0"] + a["y1"]) / 2
            by = (b["y0"] + b["y1"]) / 2
            if abs(ay - by) > height * 0.06:
                continue
            ax = (a["x0"] + a["x1"]) / 2
            bx = (b["x0"] + b["x1"]) / 2
            if abs(ax - bx) < width * 0.12:
                continue
            if min(a["area"], b["area"]) < max(a["area"], b["area"]) * 0.45:
                continue
            pair = sorted([a, b], key=lambda blob: blob["x0"])
            break
        if pair:
            break
    if not pair:
        print(name, "глаза не найдены, пятен:", len(blobs))
        return None
    eyes = pair

    left, right = eyes
    skin = skin_color(image, left)

    def describe(eye):
        return {
            "cx": (eye["x0"] + eye["x1"]) / 2 / width,
            "cy": (eye["y0"] + eye["y1"]) / 2 / height,
            "w": (eye["x1"] - eye["x0"] + 1) / width,
            "h": (eye["y1"] - eye["y0"] + 1) / height,
        }

    result = {"left": describe(left), "right": describe(right), "skin": skin}
    print(
        "%-10s левый(%.3f, %.3f) правый(%.3f, %.3f) размер(%.3f x %.3f) кожа #%02X%02X%02X"
        % (
            name,
            result["left"]["cx"], result["left"]["cy"],
            result["right"]["cx"], result["right"]["cy"],
            max(result["left"]["w"], result["right"]["w"]),
            max(result["left"]["h"], result["right"]["h"]),
            skin[0], skin[1], skin[2],
        )
    )

    # Превью: так лицо будет выглядеть с закрытыми глазами.
    preview = image.copy()
    draw = ImageDraw.Draw(preview)
    for eye in (result["left"], result["right"]):
        cx, cy = eye["cx"] * width, eye["cy"] * height
        half_w = max(eye["w"] * width, 0.055 * width) * 0.75
        half_h = max(eye["h"] * height, 0.055 * height) * 0.72
        draw.ellipse(
            [cx - half_w * 1.25, cy - half_h * 1.45, cx + half_w * 1.25, cy + half_h * 1.15],
            fill=skin,
        )
        draw.arc(
            [cx - half_w, cy - half_h * 0.9, cx + half_w, cy + half_h * 1.1],
            start=200, end=340, fill=(70, 50, 40), width=max(2, int(width * 0.008)),
        )
    os.makedirs(PREVIEW_DIR, exist_ok=True)
    preview.save(os.path.join(PREVIEW_DIR, name + "_blink.png"))
    return result


if __name__ == "__main__":
    for face in FACES:
        analyse(face)
    print("Превью:", PREVIEW_DIR)
