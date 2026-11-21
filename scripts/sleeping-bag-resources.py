#!/usr/bin/env python3
"""Portable floor sleeping bags, using vanilla wool textures and all bed colors."""
from pathlib import Path
import json
MOD='fantasy_feudalism_items'
COLORS='white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black'.split()
def generate(root):
    root=Path(root);resources=root/'src/main/resources'
    def write(path,data):
        p=resources/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
    def cube(start,end,texture,tinted=False):
        return {'from':start,'to':end,'faces':{s:{'texture':texture,'uv':[0,0,16,16],**({'tintindex':0} if tinted else {})} for s in ['north','south','east','west','up','down']}}
    write(f'assets/{MOD}/models/item/sleeping_bag.json',{'parent':'minecraft:block/block','textures':{'fabric':'minecraft:block/white_wool','particle':'minecraft:block/white_wool'},'display':{'gui':{'rotation':[20,45,0],'scale':[1,1,1]},'firstperson_righthand':{'rotation':[0,45,0],'scale':[0.5,0.5,0.5]},'firstperson_lefthand':{'rotation':[0,225,0],'scale':[0.5,0.5,0.5]}},'elements':[cube([6.5,2.5,6.5],[9.5,13.5,9.5],'#fabric',True)]})
    lang=json.loads((resources/f'assets/{MOD}/lang/en_us.json').read_text())
    for color in COLORS:
        name=color+'_sleeping_bag';label=' '.join(x.capitalize() for x in color.split('_'))+' Sleeping Bag';lang['block.'+MOD+'.'+name]=label
        write(f'assets/{MOD}/models/item/{name}.json',{'parent':MOD+':item/sleeping_bag'})
        for part in ['foot','head']:
            elements=[cube([0,0,0],[16,2.5,16],'#fabric')]
            if part=='head':elements.append(cube([1,2.5,1],[15,3,6],'#pillow'))
            write(f'assets/{MOD}/models/block/{name}_{part}.json',{'parent':'minecraft:block/block','textures':{'fabric':'minecraft:block/'+color+'_wool','pillow':'minecraft:block/white_wool','particle':'minecraft:block/'+color+'_wool'},'elements':elements})
        variants={f'facing={facing},part={part}':{'model':MOD+':block/'+name+'_'+part,**({'y':rotation} if rotation else {})} for facing,rotation in [('north',0),('east',90),('south',180),('west',270)] for part in ['foot','head']}
        write(f'assets/{MOD}/blockstates/{name}.json',{'variants':variants})
        write(f'data/{MOD}/recipe/{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['WWW','LLL'],'key':{'W':{'item':'minecraft:'+color+'_wool'},'L':{'item':'minecraft:leather'}},'result':{'id':MOD+':'+name,'count':1}})
        write(f'data/{MOD}/advancement/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_wool':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:'+color+'_wool'}]}},'has_the_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':MOD+':'+name}}},'requirements':[['has_wool','has_the_recipe']],'rewards':{'recipes':[MOD+':'+name]}})
        write(f'data/{MOD}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'conditions':[{'condition':'minecraft:block_state_property','block':MOD+':'+name,'properties':{'part':'foot'}},{'condition':'minecraft:survives_explosion'}],'entries':[{'type':'minecraft:item','name':MOD+':'+name}]}]})
    values=[MOD+':'+c+'_sleeping_bag' for c in COLORS]
    write(f'data/{MOD}/tags/item/sleeping_bags.json',{'replace':False,'values':values})
    p=resources/f'data/{MOD}/tags/item/items.json';items=json.loads(p.read_text());items['values']=list(dict.fromkeys(items['values']+values));write(f'data/{MOD}/tags/item/items.json',items)
    write(f'data/{MOD}/advancement/sleeping_bag.json',{'parent':MOD+':root','display':{'icon':{'id':MOD+':white_sleeping_bag'},'title':'Portable Rest','description':'Obtain a sleeping bag'},'criteria':{'sleeping_bag':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'#'+MOD+':sleeping_bags'}]}}}})
    lang.update({'tooltip.'+MOD+'.sleeping_bag':'10-second sleep. Does not set your spawn point.','message.'+MOD+'.remove_sleeping_bag':'Remove the sleeping bag before removing the saddlebags.','container.'+MOD+'.bags':'Bags','container.'+MOD+'.bedroll':'Bedroll'})
    write(f'assets/{MOD}/lang/en_us.json',lang)
if __name__=='__main__':generate(Path(__file__).resolve().parents[1])
