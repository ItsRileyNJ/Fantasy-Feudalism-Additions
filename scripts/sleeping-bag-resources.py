#!/usr/bin/env python3
"""Generate sleeping bag models, dye palettes, and gameplay resources."""
from pathlib import Path
import json
import random
import struct
import zlib
MOD='fantasy_feudalism_items'
COLORS='white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black'.split()
# Familiar wool midtones. The fabric, stitching, and folds use shades of these colors.
PALETTES = dict(zip(COLORS, (
    0xE4E5E5, 0xEB7F17, 0xBC469E, 0x3DAFD6, 0xF5C62D, 0x74B91B,
    0xEC8DAA, 0x414649, 0x8D8D86, 0x168C8E, 0x7B2CAE, 0x373B9C,
    0x734A31, 0x556D1C, 0xA52B27, 0x212125,
)))
ATLAS_SIZE = 64
FOOT, HEAD, UNDERSIDE = (2, 2), (22, 2), (42, 2)
HEM, PILLOW, PILLOW_EDGE = (2, 22), (22, 22), (42, 22)
ROLL_SIDE, ROLL_FOLD, ROLL_END, BUCKLE = (2, 30), (10, 30), (18, 30), (26, 30)
FACE_AXES = {'north': (0, 1), 'south': (0, 1), 'east': (2, 1),
             'west': (2, 1), 'up': (0, 2), 'down': (0, 2)}


def cube(start, end, origins, texture='#fabric'):
    faces = {}
    for side, (u, v) in origins.items():
        a, b = FACE_AXES[side]
        width, height = end[a] - start[a], end[b] - start[b]
        # JSON uses a normalized 0–16 UV range. Sample one texel per model unit.
        faces[side] = {'texture': texture, 'uv': [
            16 * u / ATLAS_SIZE, 16 * v / ATLAS_SIZE,
            16 * (u + width) / ATLAS_SIZE, 16 * (v + height) / ATLAS_SIZE,
        ]}
    return {'from': start, 'to': end, 'faces': faces}


def roll(start, end):
    return cube(start, end, {
        'north': ROLL_SIDE, 'south': ROLL_SIDE, 'east': ROLL_FOLD, 'west': ROLL_SIDE,
        'up': ROLL_END, 'down': ROLL_END,
    })


def floor_elements(part):
    cover = cube([0, 0, 0], [16, 2.5, 16], {
        'up': FOOT if part == 'foot' else HEAD, 'down': UNDERSIDE,
        'west': HEM, 'east': HEM, 'north': HEM, 'south': HEM,
    })
    if part == 'foot':
        return [cover]
    pillow = cube([1, 2.5, 1], [15, 3, 6], {
        'up': PILLOW, 'down': PILLOW, 'north': PILLOW_EDGE, 'south': PILLOW_EDGE,
        'west': PILLOW_EDGE, 'east': PILLOW_EDGE,
    })
    return [cover, pillow]


def write_texture(path, base):
    rng = random.Random(42)
    pixels = bytearray(base.to_bytes(3, 'big') * (ATLAS_SIZE * ATLAS_SIZE))

    def paint(u, v, width, height, color, shade):
        channels = (color >> 16, (color >> 8) & 255, color & 255)
        def shaded_color(x, y):
            factor = shade(x, y) / 100
            return bytes(min(255, round(c * factor)) for c in channels)

        colors = [[shaded_color(x, y) for x in range(width)] for y in range(height)]
        # Extend the edge texels into the gutter for block atlas mipmapping.
        for y in range(-2, height + 2):
            for x in range(-2, width + 2):
                index = ((v + y) * ATLAS_SIZE + u + x) * 3
                pixels[index:index + 3] = colors[max(0, min(height - 1, y))][max(0, min(width - 1, x))]

    def blanket(x, y, outside_edge):
        if x in (0, 15) or y == outside_edge:
            return 73
        if x in (1, 14) or y == outside_edge + (1 if outside_edge == 0 else -1):
            return 108 if (x + y) % 3 else 94
        return 98 + rng.choice((-4, -2, 0, 0, 2, 4))

    paint(*FOOT, 16, 16, base, lambda x, y: blanket(x, y, 15))
    paint(*HEAD, 16, 16, base, lambda x, y: blanket(x, y, 0))
    paint(*UNDERSIDE, 16, 16, 0x64452E, lambda x, y: 93 + rng.choice((-4, 0, 4)))
    paint(*HEM, 16, 3, base, lambda x, y: (84, 96, 58)[y] + (x % 3 - 1) * 3)
    paint(*PILLOW, 14, 5, 0xE4DFD4,
          lambda x, y: 85 if x in (0, 13) or y == 4 else (102, 107, 104, 98)[y])
    paint(*PILLOW_EDGE, 14, 1, 0xBCB5A7, lambda x, y: 95 + x % 3 * 3)
    paint(*ROLL_SIDE, 3, 11, base, lambda x, y: (85, 105, 94)[x] - (8 if y in (0, 10) else 0))
    paint(*ROLL_FOLD, 3, 11, base, lambda x, y: (72, 91, 102)[x] - (8 if y in (0, 10) else 0))
    # A three-texel cross-section gives the roll a visible folded end at vanilla scale.
    spiral = ((114, 107, 96), (85, 67, 91), (79, 84, 88))
    paint(*ROLL_END, 3, 3, base, lambda x, y: spiral[y][x])
    buckle = ((125, 95), (81, 60))
    paint(*BUCKLE, 2, 2, 0xB8C2CB, lambda x, y: buckle[y][x])

    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data))

    stride = ATLAS_SIZE * 3
    rows = b''.join(b'\0' + pixels[i:i + stride] for i in range(0, len(pixels), stride))
    header = struct.pack('>IIBBBBB', ATLAS_SIZE, ATLAS_SIZE, 8, 2, 0, 0, 0)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', header)
                     + chunk(b'IDAT', zlib.compress(rows)) + chunk(b'IEND', b''))


def generate(root):
    root=Path(root);resources=root/'src/main/resources'
    def write(path,data):
        p=resources/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
    assets = f'assets/{MOD}'
    write(f'{assets}/models/item/sleeping_bag.json', {
        'parent': 'minecraft:block/block',
        'textures': {'particle': '#fabric'},
        'display': {
            'gui': {'rotation': [20, 45, 0], 'scale': [1, 1, 1]},
            'firstperson_righthand': {'rotation': [0, 45, 0], 'scale': [0.5, 0.5, 0.5]},
            'firstperson_lefthand': {'rotation': [0, 225, 0], 'scale': [0.5, 0.5, 0.5]},
        },
        'elements': [roll([6.5, 2.5, 6.5], [9.5, 13.5, 9.5])],
    })
    for part in ('foot', 'head'):
        write(f'{assets}/models/block/sleeping_bag_{part}.json', {
            'parent': 'minecraft:block/block', 'textures': {'particle': '#fabric'},
            'elements': floor_elements(part),
        })
    lang=json.loads((resources/f'{assets}/lang/en_us.json').read_text())
    for color in COLORS:
        name=color+'_sleeping_bag';label=' '.join(x.capitalize() for x in color.split('_'))+' Bedroll';lang['block.'+MOD+'.'+name]=label
        texture = f'{MOD}:block/sleeping_bag/{color}'
        write_texture(resources / f'{assets}/textures/block/sleeping_bag/{color}.png', PALETTES[color])
        for suffix in ('', '_equipped'):
            write(f'{assets}/models/item/{name}{suffix}.json', {
                'parent': f'{MOD}:item/sleeping_bag{suffix}', 'textures': {'fabric': texture},
            })
        for part in ('foot', 'head'):
            write(f'{assets}/models/block/{name}_{part}.json', {
                'parent': f'{MOD}:block/sleeping_bag_{part}', 'textures': {'fabric': texture},
            })
        variants={f'facing={facing},part={part}':{'model':MOD+':block/'+name+'_'+part,**({'y':rotation} if rotation else {})} for facing,rotation in [('north',0),('east',90),('south',180),('west',270)] for part in ['foot','head']}
        write(f'assets/{MOD}/blockstates/{name}.json',{'variants':variants})
        write(f'data/{MOD}/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['WWW','LLL'],'key':{'W':{'item':'minecraft:'+color+'_wool'},'L':{'item':'minecraft:leather'}},'result':{'id':MOD+':'+name,'count':1}})
        write(f'data/{MOD}/advancement/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_wool':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:'+color+'_wool'}]}},'has_the_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':MOD+':'+name}}},'requirements':[['has_wool','has_the_recipe']],'rewards':{'recipes':[MOD+':'+name]}})
        write(f'data/{MOD}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:block_state_property','block':MOD+':'+name,'properties':{'part':'foot'}},{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':MOD+':'+name}]}]})
    values=[MOD+':'+c+'_sleeping_bag' for c in COLORS]
    write(f'data/{MOD}/tags/item/sleeping_bags.json',{'replace':False,'values':values})
    p=resources/f'data/{MOD}/tags/item/items.json';items=json.loads(p.read_text());items['values']=list(dict.fromkeys(items['values']+values));write(f'data/{MOD}/tags/item/items.json',items)
    write(f'data/{MOD}/advancement/sleeping_bag.json',{'parent':MOD+':root','display':{'icon':{'id':MOD+':white_sleeping_bag'},'title':'Portable Rest','description':'Obtain a bedroll'},'criteria':{'sleeping_bag':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'#'+MOD+':sleeping_bags'}]}}}})
    lang.update({'tooltip.'+MOD+'.sleeping_bag':'Does not set your spawn point.','message.'+MOD+'.remove_sleeping_bag':'Remove the bedroll before removing the saddlebags.','container.'+MOD+'.bags':'Bags','container.'+MOD+'.bedroll':'Bedroll'})
    write(f'assets/{MOD}/lang/en_us.json',lang)
if __name__=='__main__':generate(Path(__file__).resolve().parents[1])
