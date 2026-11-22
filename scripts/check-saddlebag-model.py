#!/usr/bin/env python3
"""Independently compare the exported equipment against the user's Java reference."""
from pathlib import Path
from itertools import product
from collections import Counter
import json, math, re

ROOT=Path(__file__).resolve().parents[1]
def numbers(text):return [float(v.strip().rstrip('Ff')) for v in text.split(',')]
def bounds(points):return tuple(round(fn(p[axis] for p in points),5) for fn in (min,max) for axis in range(3))
def check():
    java=(ROOT/'models/Donkey.java').read_text();expected=[]
    body=numbers(re.search(r'PartDefinition Body = .*?PartPose.offset\(([^)]+)',java,re.S).group(1))
    for name in ('BagL','BagR'):
        part=re.search(r'PartDefinition '+name+r' = Body\.addOrReplaceChild\(.*?;',java,re.S).group()
        x,y,z,rx,ry,rz=numbers(re.search(r'PartPose.offsetAndRotation\(([^)]+)',part).group(1));assert rx==rz==0
        # The Java reference rounds pi/2 to four decimals. Snap quarter-turns.
        c,s=round(math.cos(ry)),round(math.sin(ry))
        for raw in re.findall(r'\.addBox\(([-\d.Ff, ]+), new CubeDeformation',part):
            a,b,d,w,h,depth=numbers(raw)
            expected.append(bounds([(c*u+s*t+x+body[0],v+y+body[1],-s*u+c*t+z+body[2])
                                    for u,v,t in product((a,a+w),(b,b+h),(d,d+depth))]))
    models=ROOT/'src/main/resources/assets/fantasy_feudalism_items/models/item'
    equipped=json.loads((models/'saddlebag_equipped.json').read_text());roll=json.loads((models/'sleeping_bag_equipped.json').read_text());actual=[]
    for e in equipped['elements']+roll['elements']:
        # Undo ItemRenderer centering/entity Y/Z flip and apply the native body pivot.
        actual.append(bounds([(p[0]-8,11-(p[1]-8),5-(p[2]-8)) for p in product(*zip(e['from'],e['to']))]))
    assert len(actual)==len(expected)==14
    assert Counter(actual)==Counter(expected),'Mounted geometry differs from the supplied Java reference'
    inventory=json.loads((models/'saddlebag.json').read_text())
    assert len(inventory['elements'])==len(equipped['elements'])==13 and len(roll['elements'])==1
    ratios=[]
    for source,icon in zip(equipped['elements'],inventory['elements']):
        assert set(source['faces'])==set(icon['faces'])=={'north','south','east','west','up','down'}
        for axis in range(3):ratios.append((icon['to'][axis]-icon['from'][axis])/(source['to'][axis]-source['from'][axis]))
    assert max(ratios)-min(ratios)<1e-5,'Inventory mesh was distorted instead of uniformly scaled'
    for axis in range(3):
        low=min(e['from'][axis] for e in inventory['elements']);high=max(e['to'][axis] for e in inventory['elements'])
        assert abs((low+high)/2-8)<1e-5 and high-low<=14.00001
    half=json.loads((models/'saddlebag_half.json').read_text())
    assert len(half['elements'])==7
    for model in (equipped,inventory,half):
        assert model['textures']['material']=='fantasy_feudalism_items:item/saddlebag_materials'
        for part in model['elements']:
            for face in part['faces'].values():
                assert face['texture']=='#material' and 'tintindex' not in face
                assert all(0<=c<=16 for c in face['uv'])
                assert face.get('rotation',0) in (0,90,180,270)
    original=json.loads((ROOT/'models/Saddlebag.bbmodel').read_text())
    textured=json.loads((ROOT/'models/Saddlebag-textured.bbmodel').read_text())
    assert original['groups']==textured['groups'] and original['outliner']==textured['outliner']
    for source,result in zip(original['elements'],textured['elements']):
        for key in ('uuid','from','to','origin','rotation'):assert source.get(key)==result.get(key)
    print('14 original cubes/pivots preserved; full and half bags use complete untinted, in-bounds texture mappings.')
if __name__=='__main__':check()
