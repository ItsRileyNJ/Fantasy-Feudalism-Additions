#!/usr/bin/env python3
"""Bake the supplied entity-model groups into one body-attached saddlebag model."""
from pathlib import Path
import base64, json, math
from itertools import product
from copy import deepcopy

MOD = 'fantasy_feudalism_items'
PEG = '7b1d31c5-c651-3052-5840-24af49eb1301'
ROLL = 'c81b87a2-78bd-e66b-886a-d06462ccdc8b'
NORMALS={'north':(0,0,-1),'south':(0,0,1),'east':(1,0,0),'west':(-1,0,0),'up':(0,1,0),'down':(0,-1,0)}
MATERIALS={'leather':(0,0),'strap':(8,0),'metal':(0,8),'lining':(8,8)}

def face_uv(material,side,size):
    a,b={'north':(0,1),'south':(0,1),'east':(2,1),'west':(2,1),'up':(0,2),'down':(0,2)}[side]
    w,h=size[a],size[b];scale=7.75/max(w,h);u,v=MATERIALS[material]
    return [round(u+4-w*scale/2,6),round(v+4-h*scale/2,6),round(u+4+w*scale/2,6),round(v+4+h*scale/2,6)]

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
    parts=[];left=[];roll=[];peg=None;textured=deepcopy(source)
    textured_cubes={e['uuid']:e for e in textured['elements']}
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
                # Official Java export: X=-BB.X, Y=24-BB.Y, Z=BB.Z.
                # Attach to vanilla HorseModel.body at (0,11,5), rather than
                # pretending the reference Donkey Body pivot is the same.
                native=[-p[0],24-p[1],p[2]]
                return [(native[i]-(0,11,5)[i])/16 for i in range(3)]
            points=[transform(p) for p in product(*zip(cube['from'],cube['to']))]
            if node==PEG:
                # Use the peg's outer/front end, centered vertically and across its width.
                peg=transform([(cube['from'][0]+cube['to'][0])/2,(cube['from'][1]+cube['to'][1])/2,cube['to'][2]])
            # ItemRenderer centers at 8 and the existing entity layer flips Y/Z.
            converted=[[8+p[0]*16,8-p[1]*16,8-p[2]*16] for p in points]
            bounds=[[round(min(p[i] for p in converted),6) for i in range(3)],[round(max(p[i] for p in converted),6) for i in range(3)]]
            size=[b-a for a,b in zip(cube['from'],cube['to'])]
            material='leather' if cube['name'] in ('BagL','BagR') else 'metal' if size==[2,2,1] or node==PEG else 'strap'
            faces={};origin=transform((0,0,0));bbfaces={}
            for side,normal in NORMALS.items():
                # Rotate each face along with its cube; preserve the original mesh/pivots.
                end=transform(normal);direction=tuple(round((end[i]-origin[i])*16)*(1 if i==0 else -1) for i in range(3))
                target=next(name for name,n in NORMALS.items() if n==direction)
                face_material='lining' if material=='leather' and side=='north' else material
                uv=face_uv(face_material,side,size)
                faces[target]={'texture':'#material','uv':uv}
                if target in ('up','down'):
                    angle=sum(g.get('rotation',[0,0,0])[1] for g in parents)+cube.get('rotation',[0,0,0])[1]+180
                    faces[target]['rotation']=int((-angle if target=='up' else angle)%360)
                bbfaces[side]={'uv':[round(c*8,6) for c in uv],'texture':0}
            if node!=ROLL:
                textured_cubes[node].update(box_uv=False,mirror_uv=False,faces=bbfaces)
            if node==ROLL:
                roll.append({'from':bounds[0],'to':bounds[1],'faces':{side:{'texture':'#fabric','uv':[0,0,16,16],'tintindex':0} for side in NORMALS}})
            else:
                part={'from':bounds[0],'to':bounds[1],'faces':faces};parts.append(part)
                if any(g['name']=='BagL' for g in parents):left.append(part)
    walk(source['outliner'],[])
    assert len(parts)+len(roll)==len(cubes) and len(roll)==1 and peg is not None
    texture=MOD+':item/saddlebag_materials'
    model={'parent':'minecraft:block/block','textures':{'material':texture,'particle':texture},'display':{'gui':{'rotation':[25,140,0],'scale':[0.75,0.75,0.75]},'ground':{'translation':[0,3,0],'scale':[0.5,0.5,0.5]},'thirdperson_righthand':{'rotation':[0,90,0],'scale':[0.5,0.5,0.5]},'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]}},'elements':parts}
    sleeping_model={'parent':'minecraft:block/block','textures':{'fabric':'minecraft:block/white_wool','particle':'minecraft:block/white_wool'},'elements':roll}
    (root/f'src/main/resources/assets/{MOD}/models/item/sleeping_bag_equipped.json').write_text(json.dumps(sleeping_model,indent=2)+'\n')
    mounted=deepcopy(model);mounted.pop('display')
    dest=root/f'src/main/resources/assets/{MOD}/models/item/saddlebag_equipped.json';dest.write_text(json.dumps(mounted,indent=2)+'\n')
    # Inventory/held geometry is centered independently, with uniform scale.
    low=[min(p['from'][i] for p in parts) for i in range(3)]
    high=[max(p['to'][i] for p in parts) for i in range(3)]
    center=[(a+b)/2 for a,b in zip(low,high)];scale=14/max(b-a for a,b in zip(low,high))
    inventory=deepcopy(model)
    for part in inventory['elements']:
        for corner in ('from','to'):part[corner]=[round(8+(n-center[i])*scale,6) for i,n in enumerate(part[corner])]
    inventory['display']['gui']={'rotation':[25,140,0],'scale':[0.85,0.85,0.85]}
    inventory['display']['firstperson_righthand']={'rotation':[0,45,0],'translation':[0,0,0],'scale':[0.5,0.5,0.5]}
    inventory['display']['firstperson_lefthand']={'rotation':[0,225,0],'translation':[0,0,0],'scale':[0.5,0.5,0.5]}
    dest=root/f'src/main/resources/assets/{MOD}/models/item/saddlebag.json';dest.write_text(json.dumps(inventory,indent=2)+'\n')
    # The crafting half uses the same finished leather and fittings, without the other side.
    half=deepcopy(inventory);half['elements']=deepcopy(left)
    low=[min(p['from'][i] for p in left) for i in range(3)];high=[max(p['to'][i] for p in left) for i in range(3)]
    center=[(a+b)/2 for a,b in zip(low,high)];scale=14/max(b-a for a,b in zip(low,high))
    for part in half['elements']:
        for corner in ('from','to'):part[corner]=[round(8+(n-center[i])*scale,6) for i,n in enumerate(part[corner])]
    (root/f'src/main/resources/assets/{MOD}/models/item/saddlebag_half.json').write_text(json.dumps(half,indent=2)+'\n')
    # An editable Blockbench copy carries the finished UVs and embedded atlas.
    png=(root/f'src/main/resources/assets/{MOD}/textures/item/saddlebag_materials.png').read_bytes()
    textured['meta']['box_uv']=False;textured['resolution']={'width':128,'height':128}
    textured['textures']=[{'name':'saddlebag_materials.png','id':'0','uuid':'c3487d30-bda6-4c42-9270-bd5f85e3ab25','width':128,'height':128,'uv_width':128,'uv_height':128,'render_mode':'default','render_sides':'auto','visible':True,'source':'data:image/png;base64,'+base64.b64encode(png).decode()}]
    # The optional sleeping bag retains its separate color-tinted runtime model.
    textured_cubes[ROLL].update(box_uv=False,mirror_uv=False,faces={side:{'uv':[65,65,127,127],'texture':0} for side in NORMALS})
    (root/'models/Saddlebag-textured.bbmodel').write_text(json.dumps(textured,indent=2)+'\n')
    java=root/'src/main/java/com/fantasyfeudalism/items/client/SaddlebagAttachment.java'
    java.write_text('''package com.fantasyfeudalism.items.client;
/** Generated from the marked side peg in models/Saddlebag.bbmodel. Body-local units. */
public final class SaddlebagAttachment {
    private SaddlebagAttachment() {}
    public static final double X = %s, Y = %s, Z = %s;
    public static final float OUTWARD_TILT = 20, REARWARD_TILT = 5;
    public static final float CHAIN_LENGTH = 0.22f, LANTERN_SCALE = 0.6f;
}
''' % tuple(peg))
    return parts,peg

if __name__=='__main__':generate(Path(__file__).resolve().parents[1])
