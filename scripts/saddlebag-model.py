#!/usr/bin/env python3
"""Bake the supplied entity-model groups into one body-attached saddlebag model."""
from pathlib import Path
import json, math
from itertools import product

MOD = 'fantasy_feudalism_items'
PEG = '7b1d31c5-c651-3052-5840-24af49eb1301'

def rotate(point, origin, angles):
    p = [a-b for a,b in zip(point,origin)]
    for axis, angle in enumerate(angles):
        if angle % 90: raise ValueError('Only quarter-turn source rotations can be baked into axis-aligned cubes')
        c,s = round(math.cos(math.radians(angle))),round(math.sin(math.radians(angle)))
        a,b = [(1,2),(2,0),(0,1)][axis]
        p[a],p[b] = c*p[a]-s*p[b],s*p[a]+c*p[b]
    return [a+b for a,b in zip(p,origin)]

def generate(root):
    root=Path(root);source=json.loads((root/'models/Saddlebag.bbmodel').read_text())
    groups={g['uuid']:g for g in source['groups']};cubes={e['uuid']:e for e in source['elements'] if e['type']=='cube' and e.get('export',True)}
    body=groups[source['outliner'][0]['uuid']]['origin'];parts=[];peg=None
    def walk(nodes,parents):
        nonlocal peg
        for node in nodes:
            if isinstance(node,dict):
                group=groups[node['uuid']]
                if group.get('export',True):walk(node.get('children',[]),parents+[group])
                continue
            if node not in cubes:continue
            cube=cubes[node]
            def transform(p):
                p=rotate(p,cube.get('origin',[0,0,0]),cube.get('rotation',[0,0,0]))
                for g in reversed(parents):p=rotate(p,g['origin'],g.get('rotation',[0,0,0]))
                # Blockbench's entity frame faces the opposite X/Z direction;
                # subtract the supplied Body pivot before attaching to HorseModel.body.
                return [-(p[i]-body[i])/16 for i in range(3)]
            points=[transform(p) for p in product(*zip(cube['from'],cube['to']))]
            if node==PEG:
                # Use the peg's outer/front end, centered vertically and across its width.
                peg=transform([(cube['from'][0]+cube['to'][0])/2,(cube['from'][1]+cube['to'][1])/2,cube['to'][2]])
            # ItemRenderer centers at 8 and the existing entity layer flips Y/Z.
            converted=[[8+p[0]*16,8-p[1]*16,8-p[2]*16] for p in points]
            bounds=[[round(min(p[i] for p in converted),6) for i in range(3)],[round(max(p[i] for p in converted),6) for i in range(3)]]
            size=[b-a for a,b in zip(cube['from'],cube['to'])]
            material='leather' if cube['name'] in ('BagL','BagR') else 'metal' if size==[2,2,1] else 'strap'
            faces={side:{'texture':'#'+material,'uv':[0,0,16,16],**({'tintindex':0} if material=='leather' else {})} for side in ('north','south','east','west','up','down')}
            parts.append({'from':bounds[0],'to':bounds[1],'faces':faces})
    walk(source['outliner'],[])
    assert len(parts)==len(cubes) and peg is not None
    model={'parent':'minecraft:block/block','textures':{'leather':'minecraft:block/white_wool','strap':'minecraft:block/brown_terracotta','metal':'minecraft:block/iron_block','particle':'minecraft:block/brown_terracotta'},'display':{'gui':{'rotation':[25,140,0],'scale':[0.75,0.75,0.75]},'ground':{'translation':[0,3,0],'scale':[0.5,0.5,0.5]},'thirdperson_righthand':{'rotation':[0,90,0],'scale':[0.5,0.5,0.5]},'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]}},'elements':parts}
    dest=root/f'src/main/resources/assets/{MOD}/models/item/saddlebag.json';dest.write_text(json.dumps(model,indent=2)+'\n')
    java=root/'src/main/java/com/fantasyfeudalism/items/client/SaddlebagAttachment.java'
    java.write_text('''package com.fantasyfeudalism.items.client;
/** Generated from the marked side peg in models/Saddlebag.bbmodel. Body-local units. */
public final class SaddlebagAttachment {
    private SaddlebagAttachment() {}
    public static final double X = %s, Y = %s, Z = %s;
    public static final float OUTWARD_TILT = 20, REARWARD_TILT = 20;
    public static final float CHAIN_LENGTH = 0.22f, LANTERN_SCALE = 0.45f;
}
''' % tuple(peg))
    return parts,peg

if __name__=='__main__':generate(Path(__file__).resolve().parents[1])
