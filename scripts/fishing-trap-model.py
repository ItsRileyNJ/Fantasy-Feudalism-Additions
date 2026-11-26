#!/usr/bin/env python3
"""Bake the Blockbench fish trap with vanilla stripped spruce textures."""
import json
from pathlib import Path

FACES = {
    'north': (0, 1, 2), 'south': (0, 1, 2),
    'west': (2, 1, 0), 'east': (2, 1, 0),
    'up': (0, 2, 1), 'down': (0, 2, 1),
}


def generate(root):
    source = json.loads((root / 'models/FishingTrap.bbmodel').read_text())
    elements = []
    for cube in source['elements']:
        start, end = cube['from'], cube['to']
        size = [b - a for a, b in zip(start, end)]
        grain = max(range(3), key=size.__getitem__)
        element = {'from': start, 'to': end, 'faces': {}}
        if 'rotation' in cube:
            axis, angle = next((axis, angle) for axis, angle in zip('xyz', cube['rotation']) if angle)
            element['rotation'] = {'origin': cube['origin'], 'axis': axis, 'angle': angle}
        for side, (u, v, normal) in FACES.items():
            across = grain == u
            width, height = (size[v], size[u]) if across else (size[u], size[v])
            # Center the cropped texels. One UV unit maps to one source pixel.
            face = {'uv': [8-width/2, 8-height/2, 8+width/2, 8+height/2],
                    'texture': '#end' if grain == normal else '#wood'}
            if across:
                face['rotation'] = 90
            element['faces'][side] = face
        elements.append(element)
    textures = {'wood': 'minecraft:block/stripped_spruce_log',
                'end': 'minecraft:block/stripped_spruce_log_top', 'particle': '#wood'}
    model = {'parent': 'minecraft:block/block', 'textures': textures, 'elements': elements}
    output = root / 'src/main/resources/assets/fantasy_feudalism_items/models/block/fishing_trap.json'
    output.write_text(json.dumps(model, indent=2) + '\n')


if __name__ == '__main__':
    generate(Path(__file__).resolve().parents[1])
