# Validation — 2026-10-30

Target: Minecraft 1.21.1, NeoForge 21.1.262, Java 21.

| Run | Result |
| --- | --- |
| Dedicated GameTest server with Mercenaries 1.17.1 | 48 passed, zero failures |
| Dedicated GameTest server with neither optional mod | 46 passed, zero failures |
| Graphical client with Mercenaries 1.17.1 and LambDynamicLights 4.8.11+1.21.1 | 10 smoke checks passed |
| Release JAR inspection | JSON parsed; no GameTest classes or bundled third-party JARs |

The server suites cover brewing and delivery, private Clairvoyance targeting and exclusions, fuel and weather, soul protection and walls, milk/cauldrons, animal following, saddlebag persistence and restrictions, mounted equipment, horseshoe stats, smithing serialization, dagger penetration and enchantments, head enchanting and drops, trophy ownership and effects, fishing rolls, Spawner Eye targeting, loot gating, and compatibility trades/weapon slots. A regression test verifies mounted fuel consumption is independent of mount spawn age.

The client checks query LambDynamicLights itself for torch, lantern, soul-lantern, off/burned-out and mounted brightness; verify synced saddlebag dye and lantern switching; and open/synchronize portable storage and the mount equipment screen. The earlier Clairvoyance visual check verified an invisible sheep outlined through a stone wall and disappearance after effect removal.

These are development integration checks, not a long-running multiplayer playtest. Placeholder visuals are intentional. The cloud client had no audio device or access to online account-service endpoints; it successfully joined the local world. The supplied lighting mod also logs a nonfatal missing Pride access-transformer warning. No external mod JAR was modified.

Machine-readable reports and a mount-equipment screenshot accompany the packaged artifacts under `validation/`.

## 0.1.1 lantern compatibility fix

The supplied Supplementaries Squared 1.21-1.2.18 JAR was inspected: `LightableLanternBlock` calls the vanilla state builder and then adds its own `lit` property; `CopperLanternBlock` adds an axis afterward. Items previously added `lit` to every subclass through the vanilla builder, causing duplicate-property registration failure.

A test-only matching hierarchy reproduced the exact `has duplicate property: lit` startup failure before the patch. Items now adds its property and default only for exact vanilla `LanternBlock` instances, including vanilla soul lanterns. The patched build passed **49 dedicated-server tests with Mercenaries**, including registration and state checks for plain, lightable and copper-style subclasses. Existing vanilla fuel, toggling and weather tests also pass. Fixture blocks and test classes are excluded from the release JAR.

The complete supplied combination was subsequently verified: **Items 0.1.1 + Supplementaries Squared 1.21-1.2.18 + Moonlight 1.21.1-3.7.2 + Supplementaries 1.21.1-4.0.2**, alongside Mercenaries 1.17.1, passed **all 49 server tests**. Tests additionally check the actual copper and brass lanterns' registration, native lit defaults and light emission when on/off, and the actual crimson lantern's native state and emission. Crimson uses `attached`, not a `lit` state; Items preserves that distinction. The test-player harness negotiates mod channels before login to support Moonlight's data-sync packets.

The graphical client loaded the same combination plus LambDynamicLights 4.8.11+1.21.1, joined the local test world, and passed **all 10 client smoke checks**. It was then stopped deliberately. No additional production-code fix was needed after supplying the dependencies: the 0.1.1 JAR is byte-for-byte identical to the previously provided patch. No third-party binaries were modified or bundled. Items does not add its fuel or weather mechanics to other mods' lanterns.

## 0.1.2 rendering, synchronization and tooltip review

- Full supplied mod set, including Macaw's Lights 1.1.5: **51 server tests passed** and **14 client smoke checks passed**.
- Without optional mods: **49 server tests passed**.
- New checks verify main/offhand re-equip behavior, real hotbar changes, unrelated item changes, fuel persistence, timer-only attachment stability, cached-stack reuse and invalidation when a light switches off.
- Reviewed light predicates, dynamic-light adapters, item fuel, mount rendering and synchronization, horseshoe modifiers, soul-source lookup, trophy persistence/effects/particles, container validation, animal-following updates, fishing timers, Clairvoyance targeting and Spawner Eye search. This is a code review and regression sweep, not a guarantee that every modpack interaction is bug-free.

Rendering reads use the immutable CustomData backing tag without modifying it. State updates still use copy-on-write. Brightness checks no longer read server config or allocate copies of custom NBT. Mounted render stacks are decoded once per synchronized attachment replacement; weak entity keys avoid retaining unloaded worlds. Only appearance changes synchronize equipment visuals; the server continues persisting exact fuel. Horseshoe attribute modifiers are changed only when their value changes.

Trophies now have separate translated benefit/drawback lines and consistent instructions for owner attunement and permanent placement. Their gameplay effects are unchanged.

The graphical client was exercised with lantern search results in the creative menu, using the complete supplied mod set, and CPU profiles were captured. The severe reported frame drop was not reproduced in this cloud setup; no before/after FPS improvement is claimed. A hardware-rendered client and the user's complete modpack may behave differently. The fixes address confirmed allocation, synchronization and hand-animation issues; the user's in-game retest remains necessary to confirm the original frame-rate symptom is resolved.

## Full uploaded modpack follow-up

Ten ZIPs supplied 195 top-level mod JARs, with no missing declared required dependencies. The pack contained Items **0.1.1**; the isolated run replaced that copy with **0.1.2**. Galosphere 1.5.5 was intentionally omitted. The complete available set entered the fixture world and passed all **14 client smoke checks**. Complementary Unbound r5.9.3 was then enabled through Iris, compiled and rendered; vanilla-lantern holding and creative-menu lantern search/hover were exercised. Diagnostic JFR files and a detailed report are saved with the artifacts.

This test used generated configs and the fixture world because user configs/options/saves were not supplied. The user reports 8 GB allocation and launching through CurseForge/vanilla; the test heap was approximately 8 GB. Software rendering limits FPS comparison. The reported severe lag has not been confirmed resolved; update the uploaded 0.1.1 to 0.1.2 and compare shader-on versus shader-off behavior on the user's hardware. No further production change resulted from this run.

## 0.1.3 built-in dynamic lighting

Replaces the LambDynamicLights adapters with a client-only vanilla-renderer hook.
Sources include either hand of living entities, dropped light items, and mounted
lanterns. Fuel, toggles, and soul-light brightness use the existing item-state
logic. Immutable per-section source snapshots are safe for chunk build workers;
changed and removed sources invalidate both their previous and current areas.
Updates run every three unpaused client ticks, and world changes clear snapshots.
Packed-light merging preserves sky light and brighter existing block light.

Client smoke checks now call the built-in provider and cover distance falloff,
range limits, packed-light preservation, dropped items, both hands, mounted
lanterns, fuel exhaustion, and switching off. Historical third-party-renderer and
shader results above apply to earlier versions; they do not validate this new
rendering hook. Built-in light travels through walls and is visual only.

Validation for this change: Java 21 compilation (including client smoke sources)
and the full `build` lifecycle passed with **49/49 dedicated-server tests**, with
no optional mods installed. The updated graphical client smoke suite has not
been executed in this workspace; terrain illumination, movement/removal visuals,
and shader/alternative renderer compatibility remain unverified in-game.

### Shader guard

The render-frame and client-tick handlers query Iris (including the legacy
Oculus API) and OptiFine shader state. While a shader pack is active, the
packed-light hook returns vanilla lighting immediately, source collection stops,
and all cached sources are discarded. Sections previously affected by built-in
lighting are scheduled for rebuilding. Detection runs before pause/tick
throttling, so shader toggles while paused also clear illumination. Disabling
shaders allows fresh source collection to resume. Missing shader providers leave
lighting available; a detected provider with a broken or incompatible API fails
closed and keeps built-in lighting disabled.

`shaderCompatibilityTest` exercises isolated current/legacy Iris and OptiFine
API fixtures, toggling packs on and off, absent providers, broken calls, missing
methods and an installed Iris with no expected API. This is included in `check`;
fixtures are excluded from the release JAR. Actual shader-pack rendering and
terrain rebuilds have not been checked in a graphical client in this workspace.

Shader-guard validation: the full build passed, including all shader detection
fixture checks and all 49 dedicated-server integration tests.

### Simpler Vial of Milk cleansing

Each vial now removes exactly one randomly selected active effect in Minecraft's
`HARMFUL` category, including its full strength, duration and any hidden weaker
instance. Other harmful effects, beneficial effects (including Clairvoyance),
and neutral effects remain unchanged. With no harmful effect, drinking still
returns the bottle and restores the existing 2.5 food points without changing
any effects. Cauldron filling, brewing and creative consumption are unchanged.

Regression checks cover multiple harmful effects, repeated drinks, strong and
infinite effects, hidden weaker instances, beneficial/neutral preservation,
no active effects, stacked bottle returns and fractional food restoration.

### Emissive lights, faster updates and trophy switching

Lit normal/soul torch models and lantern light chambers use NeoForge emissive
model data for held items and placed blocks. Wall torches and hanging lanterns
are covered. Unlit models inherit ordinary vanilla geometry without emissive
data; lantern caps and chains retain normal shading. Torch items keep their
sprite silhouette using NeoForge's item-layer loader. Dynamic source snapshots
now refresh every three unpaused ticks (150 ms at 20 TPS), with shader shutdown
unchanged. Model emission is separate from dynamic illumination of surroundings.

Owned trophies explicitly handle item-bearing interactions in either hand and
replace the current attunement. Server regression tests verify empty-hand and
held-item selection, switching back, synchronized and persistent selection,
old drawback removal, previous Night Vision cleanup, ownership checks and
preservation of the active selection when an inactive trophy is destroyed.

Validation: full build, shader API checks and **53/53 dedicated-server tests**
passed. An inheritance-aware resource check passed **36 lit/unlit held/placed
model checks**, including ordinary shading on lantern caps and chains.
Graphical launch was attempted but failed at GLFW initialization: no active X11
display. The selected environment
`ccarenv_b64_Y2NhcmVudl8zN2NjYmRhNjZjMTA4MTkxOTBmYjMyZTIwMzZhMjA1Yg`
reports desktop support with state unknown; no desktop JavaScript control tool
was offered. In-game appearance remains unverified in this workspace.

### Trophy tooltip grammar, crouched nametags and stable Clairvoyance

All trophy descriptions now use consistent, grammatical wording and explicit
units. Shared instructions explain ownership, switching, exclusive attunement
and permanent placement. Breaking an inactive trophy is no longer described as
ending the active attunement. The generator and English translations agree.

The Player Trophy ignores crouching only for nametag range and name rendering.
It keeps the standing 50% range extension (96 blocks with vanilla defaults) and
standing-style visibility while crouched. Normal invisibility, camera and team
visibility checks remain in place; other trophies and non-player entities retain
vanilla behavior. A regression checks standing/crouched selection, switching away
and non-player behavior.

Clairvoyance no longer expires a target snapshot after one second or compares
client world clocks. The server replaces snapshots every five ticks; empty
snapshots clear them. Effect loss/death clears the client cache, and highlights
still require a living, affected viewer and matching world/dimension. The render
loop includes highlighted targets even when entity culling or an uncompiled
terrain section would otherwise skip them. Boss/creative/operator exclusions and
server entity-tracking limits are unchanged. Cache tests cover snapshot
replacement, explicit clearing, world changes and wrong-dimension updates.

Validation: full build and shader API checks passed, with **54/54 server tests**.
All 29 per-trophy description translations match their source text. Rendering
hooks compile and their invocation targets were checked against Minecraft's
patched sources/bytecode. The graphical client remains blocked by the missing
display described above; these results do not establish compatibility with every
shader or entity-culling mod.

### Active trophy status indicator

Each of the nine trophy types has an informational status effect named after the
trophy. The HUD and inventory effect list render its actual trophy item model as
the icon. It has no attribute modifiers or ticking gameplay effects, no added
particles, and an infinite duration while attuned. The ledger remains the source
of trophy benefits and drawbacks; applying a marker alone grants no attunement.

Selecting a trophy immediately replaces its marker. Destruction of the active
trophy and death/respawn clear it; the regular server update reconciles missing
or stale indicators, including after loading. Milk does not change attunement,
so neither milk buckets nor Vials of Milk remove its indicator. Unchanged markers
are retained rather than reapplied or synchronized every second.

Validation: full build, shader checks and **56/56 server tests** passed. Regression
coverage includes all nine selections, single-marker exclusivity, stable instance
reuse, switching, active/inactive trophy destruction, death cleanup, milk and
manual-effect removal/reconciliation. All nine localized effect names match the
trophy names. Client icon hooks compile; graphical layout remains unverified
because the environment has no active display, as documented above.

### Supplied Blockbench trophy model

`models/Trophy.bbmodel` preserves the uploaded source. All nine trophy block
models now use its four cubes unchanged: 10x2x10 base, 4x1x4 collar, 2x3x2 post,
and 8x8x8 head from [4,6,4] to [12,14,12]. The three wooden pieces preserve the
source face UVs and use stripped spruce log sides/end grain. Head faces use the
vanilla head/skull texture regions, normalized to each sheet's dimensions. Piglin
and Ender Dragon use their own head UV layouts; the generic Player Trophy uses
Steve. The supplied cube geometry is retained rather than replacing it with
vanilla skull meshes or adding ears, snouts or other mob geometry.

The block atlas adds the vanilla entity textures via single-sprite references,
without copying textures, so resource-pack replacements remain usable. Trophy
inventory models and the attunement status icons inherit these updated models.
The block interaction shape matches all four supplied cubes.

`python3 scripts/trophy-models.py` regenerates the models directly. The normal
content generator invokes the same exporter instead of writing placeholder
cubes. Validation checked all nine models against every source cube, stand UVs,
vanilla texture-file existence/dimensions, normalized head UVs, atlas references,
and inventory inheritance. Both direct and full content regeneration preserve
these assets. Full build, shader checks and **57/57 server tests** passed,
including all trophy interaction shapes. The new artwork has not been visually
verified in a running client; the earlier graphical-display limitation remains.

### Vanilla potion appearance and dedicated item sprites

The Animal Allegiance item-color handler now matches vanilla 1.21's opaque ARGB
potion tint: the liquid receives an opaque PotionContents color, and the glass
and stopper layer receives opaque white (-1). The former RGB-only values had
zero alpha, causing tinted quads to disappear. All four brewing stages retain
the vanilla potion model's liquid and bottle textures. A regression covers both
layers of all stages and normal/extended liquid-color consistency.

Trophy Mount now uses a dedicated 16x16 transparent wooden pedestal sprite rather
than the armor-stand item. Bone Dagger uses a 32x32 transparent import of the
user's cream bone blade and brown handle design, with vanilla handheld item
transforms. Source artwork is retained under `art/item-sprites/`; release textures
are under the mod's `textures/item/` directory. Images were imported using binary
alpha and nearest-neighbor downsampling with transparent margins. The original
generated files remain intact in the workspace. The content generator references
these sprites and no longer restores the armor-stand/stone-sword placeholders.

Validation: full build, shader checks and **58/58 server tests** passed. Both
imported sprites were visually inspected at inventory scale, have transparent
backgrounds, and keep all artwork within their canvases. Isolated full resource
regeneration preserves both models and textures. Actual in-game presentation
remains unverified because the graphical display limitation above persists.

### Layered Bogged and Stray heads

The Bogged skin stores important facial details on `bogged_overlay`, which was
missing from the trophy. Both skeleton variants now use their base and outer
head textures, with cutout rendering so transparent texels do not become opaque
patches. Outer cubes use the vanilla expansions (Bogged 0.2, Stray 0.25 model
units); the supplied stand and base head cubes remain unchanged. No new mushroom
or other extra decorative geometry is introduced.

The mod's Bogged and Stray head drops now use 3D models with the same skin layers
and head UVs as the trophies. The old flat placeholder PNGs are removed. Their
existing item registrations, drops and Sigil recipes are unchanged. Both base
and overlay sheets are referenced through the block atlas, retaining resource
pack compatibility. The content exporter regenerates both standalone head models
and their trophy layers, and no longer writes placeholder head sprites.

Validation: full build, shader checks and **58/58 server tests** passed. Asset
checks confirm 64x32 source sheets, correct head-face UVs, expanded overlay bounds,
cutout rendering, exact preservation of the four supplied base cubes, removal of
placeholder references and stable full regeneration. The composed Bogged face
was visually inspected and contains its missing facial detail. Full in-game 3D
rendering remains unverified under the existing display limitation.

### One standing trophy per owner and type

The placement ledger now releases a type's reservation when its trophy block is
removed. A player can place a newly crafted trophy of that type afterward, but
cannot place another while the replacement is standing. Removal frees the
original owner's slot even if someone else breaks the trophy. Other trophy types
and owners keep their own slots. Destroying the active trophy still clears its
attunement/status indicator; destroying an inactive trophy preserves the current
selection. Broken trophies still drop nothing.

Death, logout and chunk unloading do not release slots for standing trophies.
The saved-data format is retained, so existing placed trophies remain counted
and release their slots when broken under this update. This change does not
blindly reset legacy flags, which also protect standing trophies in unloaded
chunks; historical flags for trophies destroyed before this update are not
automatically distinguishable from those standing trophies.

Current tooltips, placement-denial messages, controls and rules now describe the
one-at-a-time limit instead of lifetime placement. The resource generator keeps
the replacement wording. Validation: full build, shader checks and **60/60 server
tests** passed, including duplicate rejection, replacement at a new position,
another player breaking the trophy, independent owners/types, inactive trophy
attunement, and persistence of occupied and freed slots across save/reload.

### Horseshoe material sprites

All five tiers now use the user's two-overlapping-horseshoes design instead of
material ingot/gem placeholders. A shared 32x32 neutral metal sprite preserves
identical shape, nail holes, shading and transparency across tiers. Client item
colors provide opaque copper, iron, gold, diamond and netherite palettes. This
uses vanilla item-layer tinting; no per-frame texture generation or copies are
needed. The gold jump bonus and all speed/fall modifiers and recipes are retained.
The existing Horseshoe constructor remains available for compatibility.

Source artwork is retained at `art/item-sprites/horseshoe.png`; the release sprite
is `textures/item/horseshoe.png`. The content generator references it for every
horseshoe tier. Validation: full build, shader checks and **61/61 server tests**
passed, including distinct opaque material tints, layer isolation and unchanged
bonuses. The inventory-scale sprite was visually inspected; all five generated
models use the same sprite and survive full regeneration. Full in-game color
presentation remains unverified under the existing graphical-display limitation.

### Placement orientation, upward aura and inventory hover details

Trophy blocks now store horizontal facing, chosen opposite the placing player's
heading so their fronts face that player. The direction stays fixed after
placement and supports structure rotation/mirroring. All nine exported blockstate
files include the four horizontal rotations. Older states without a facing
property use the default north orientation; no camera tracking is added.

All heads now emit colored, gravity-free rising motes. Idle trophies emit one
small, slow-rising mote per second. Active trophies emit six larger motes every
two ticks (60/second), with faster ascent, more spread and a swirling motion.
Client particle physics ensures positive upward velocity, fades particles over
their lifetime, and retains each trophy's identifying color. Particle providers
respect normal client particle limits and reuse vanilla dust sprites.

The attunement status icon's inventory hover now shows the trophy name, all
benefits and all drawbacks using the same translated descriptions as its item.
Compact layouts use NeoForge's effect-tooltip event; expanded layouts add hover
details over the effect icon after its labels render, using the actual rendered
layout positions. This does not force compact mode or change ordinary effects.
Placement instructions stay on trophy items rather than the status tooltip.

Validation: full build, shader checks and **64/64 server tests** passed. New tests
cover all nine trophies placed in all four player orientations, fixed direction
after player movement, structure rotation, every status tooltip's complete effect
summary, normal-effect isolation, rising velocities, stronger active profiles and
particle color/type packet roundtrips. Asset checks verify all 36 facing variants,
vanilla sprite references, retained layered head geometry and regeneration. The
expanded-tooltip invocation target was checked against patched Minecraft
bytecode. Full in-game GUI/particle presentation remains unverified due to the
previously documented lack of an active graphical display.

### Full Piglin skull and Enderman trophy head

The Piglin trophy now reproduces the vanilla PiglinHeadModel/PiglinModel head:
10x8x8 head, 4x4x1 snout, both 1x2x1 tusks, and both 1x5x4 ears. Each part uses
its native piglin texture region. NeoForge composite child transforms retain the
real +/-30-degree ear angles instead of approximating them with vanilla element
rotation increments. The three supplied wooden stand pieces are unchanged.

The Ender Dragon trophy's appearance now uses the 8x8 Enderman head layout and
vanilla Enderman texture. Its separate purple eye texture is added as a cutout,
emissive layer. Existing trophy IDs, names, ingredients, effects and placement
orientation are unchanged. The updated inventory and attunement icons inherit
the same models. Both changes are retained by the model/content exporter.

Validation: full build, shader checks and **64/64 server tests** passed. Asset
checks verify piglin part bounds, source UVs, ear pivots/angles, unchanged stand,
Enderman face UVs, emissive eye data, vanilla texture paths/dimensions and stable
full regeneration. The NeoForge composite loader and root-transform structure
were checked against its implementation. Full in-game appearance remains
unverified due to the existing graphical-display limitation.

### Enderman Head drop and trophy ingredient

Enderman Head is now a registered item, appears in the creative tab and item
discovery tag, and uses the existing Enderman base/eye textures in a 3D head
model at normal inventory scale. Endermen enter the same HeadDrops logic as the
other supported mobs: configurable 0.1/0.4/0.9/1.1 percent defaults at Looting
0/I/II/III, with higher levels clamped to III.

Enderman Heads are included in the Sigil-enchantable head tag and the trophy
head lookup. The Enderman Trophy's data recipe now requires a Sigil-enchanted
Enderman Head plus a Trophy Mount. Plain Enderman Heads and Dragon Heads are
rejected. The old Dragon Head entry is removed from Sigil eligibility. Existing
trophy registry IDs and placed trophies are retained for save compatibility.
Both content and model exporters preserve the new item, tags, recipe and name.

Validation: full build, shader checks and **66/66 server tests** passed. New tests
exercise Enderman drop configuration at Looting 0 through IV, zero-chance
suppression, Sigil eligibility, positive recipe assembly and rejection of plain
Enderman/enchanted Dragon heads. Asset checks cover head/eye layers, inventory
scale, item tags and stable full regeneration.

### Stackable three-stage light fuel and torch refresh crafting

Finite light items now store only a canonical 3/3, 2/3, 1/3 or empty fuel state
plus on/off state. Full lit items have no extra fuel component and stack with
fresh vanilla items. Legacy exact counters and last-burn timestamps are removed
when normalizing inventory, opened containers, clicked stacks, drops or mounted
lights. Unrelated names/components are preserved. Soul lights remain unlimited.
Tooltips show the fuel stage, empty/unlimited state and whether the light is unlit.

Continuous burn time retains the configured total, split into three equal thirds
(with at most one tick of rounding). Partial-stage progress and duplicate-tick
protection are saved on the holder/slot, not in item components. This progress
survives save/load while the holder retains the light; only the quantized stage
travels with moved/merged items or placement/drop transfers. Legacy counters and
transfers round to the upper boundary of their current stage. Placed light
countdowns remain in world saved data. Stored, unheld items pause. Mounted oil
refills and player refreshes reset their relevant countdowns.

Placed torches can be refreshed even while lit. Inventory stack servicing spends
one flint-and-steel durability use per torch, rejecting a stack if the tool is too
worn for it. The new crafting recipe accepts one flint and steel and one or more
torch slots of the same item/components; each occupied slot consumes one torch
and outputs one fully refueled torch. Its returned tool takes the corresponding
wear and disappears on its final use. Insufficient-durability batches, lanterns,
duplicate tools and incompatible named torches are rejected. Player crafting
uses vanilla's damage/enchantment handling, including creative durability rules;
non-player remainder queries use raw wear. Recipe previews don't mutate inputs.

Validation: full build, shader checks and **74/74 server tests** passed. New tests
cover canonical legacy conversion, stack equality within stages, full-item merge
with fresh torches, three-third budgets, stage changes, duplicate hooks, saved
holder counters, actual recipe lookup, multi-slot and bulk crafting, worn tools,
last-use breakage, preservation of custom names, manual/crafting wear parity and
refresh of lit placed torches. Existing mount, weather, placement, fuel, render
predicate and light-hand tests were updated to the stage contract and passed.
Recipe, unlock and tooltip regeneration checks also passed. The hover layout has
not been verified in a graphical client under the existing display limitation.

### Supplied saddlebag model and hanging lantern

`models/Saddlebag.bbmodel` preserves the original untextured model. The exporter in `scripts/saddlebag-model.py` bakes its Body/BagL/BagR group pivots and quarter-turn rotations into one paired item model attached once to the mount's animated body. All fourteen cubes are retained. Two bag bodies use dye tint 0; straps and fittings use temporary untinted vanilla materials. The main resource generator invokes this exporter and generates the peg coordinates in `SaddlebagAttachment.java`.

The lantern is attached to the small side peg identified by its source cube UUID. A vanilla chain and the equipped lantern share that pivot and lean outward and toward the rear at 20 degrees on each axis. Interpolated age and walking amplitude add gentle idle sway and stronger movement-driven jiggle. The bounded animation remains outward/rearward throughout, runs only on the client, and does not add physics or network updates. Lantern item rendering omits the inventory FIXED transform so its handle aligns with the end of the chain at the intended scale.

Validation: full build and shader compatibility checks passed with **79/79 server tests**. The native client passed **36 smoke checks**, including all 84 baked saddlebag quads and twelve tinted bag-body quads. A dyed bag and equipped lantern were visually checked on a donkey, and a six-second idle preview was captured and decoded. Structural checks covered every source cube, valid model bounds, deterministic regeneration, the marked peg, and sway direction across idle/walking amplitudes. Mule rendering uses the same body-attached layer; no separate mule screenshot was taken.

### 0.1.4 — reference-aligned saddlebag models and dedicated equipment slots

The earlier importer incorrectly inverted the front/back axis and assumed the supplied Body pivot matched vanilla's. The Java export in `models/Donkey.java` is retained as reference data, not compiled or used to replace the animal renderer. The corrected exporter uses Blockbench's native conversion (X=-x, Y=24-y, Z=z) and attaches relative to vanilla HorseModel.body at (0,11,5). `python3 scripts/check-saddlebag-model.py` independently reads all fourteen cuboids and group poses from the supplied Java and compares their final bounds to the equipped mesh. Every cube matches. The inventory mesh is separately centered and uniformly scaled; equipped rendering uses a registered standalone model, without inventory display transforms. The chain pivot is regenerated from the same corrected geometry, preserving outward/rearward tilt and sway. Materials remain temporary because the supplied BB model has no texture.

A dedicated mount screen separates Saddle, Saddlebags and Lantern slots from bag cargo and player inventory. Cargo slots are inactive, hidden, and reject inserts, pickups and shift-clicks without bags. Equipping bags immediately enables the 27-slot grid; removal hides and disables it again. Both donkey and mule behavior are covered, including absence of native chest inventory. Horse equipment keeps its separate four-slot layout.

Validation: full build and shader API checks passed; **80/80 server tests** and **39 native client checks** passed. Native visuals were inspected for the corrected equipped mesh, centered item icon, readable equipment labels, and cargo disappearance after physically removing the bag into player inventory. The client checks confirm the dedicated equipped mesh is registered and baked. This replaces the earlier incorrect transform validation, which only counted cubes and did not compare the exported coordinates to the user's Java reference.


## Sleeping bags — 0.1.5 (2026-11-20)

- Full build: **87 dedicated-server GameTests passed**, zero failures; shader API detection checks passed.
- Graphical Minecraft client: **108 smoke checks passed**. All 16 sleeping-bag item models, their tints, and both placed block halves baked successfully. Equipped/removed roll changes synchronized correctly, and the dedicated Bedroll slot enforced removing it before the saddlebags.
- Added server coverage for all matching-wool recipes, real two-block item placement, low floor shape, a single-item drop, successful/failed sleep preserving spawn (including no existing spawn), ordinary beds retaining their spawn behavior, exactly 200 versus 100 sleep ticks, timer reset, donkey/mule equipment rules, save/load, separate death drops, and vanilla Nether/End explosions.
- Independent model check: 13 permanent saddlebag cubes plus one optional sleeping-bag cube still match the supplied Donkey.java reference. Inventory geometry remains uniformly scaled. Native screenshots checked the four equipment slots and mounted roll; temporary vanilla-texture materials remain in use.
- Release JAR: JSON parsed, all sleeping-bag recipes/blockstates matched source, correct 0.1.5 metadata, no GameTest classes or nested third-party JARs.

This update was exercised with the base NeoForge client/server. The full external modpack and shader pack were not rerun for this feature.


## Mount inventory redesign — 0.1.6 (2026-11-20)

The mount screen now uses a compact vanilla-style equipment overview with a live mount preview, health, ghost item icons, a locked sleeping-bag slot when bags are absent, and localized hover instructions. The requested storage prompt was removed. Horses use a 168-pixel panel; donkeys/mules use 180 pixels without bags and 236 with all 27 cargo slots. The same open screen expands/collapses as bags are equipped/removed. Equipment/cargo slots retain their identity, and network slot indices remain stable when player slots move.

- Full build: **88 server GameTests passed**, zero failures; shader API detection checks passed.
- Graphical client: **115 smoke checks passed**, including horse, fitted donkey, unfitted mule, live mule expansion/collapse, equipment restrictions, cargo synchronization and viewport fit at 427×240 GUI resolution. Screenshots cover both cargo states and horses; locked-slot hover text was checked separately.
- The new server regression equips a filled bag with shift-click, verifies storage/reflow, removes it, checks the stored items and confirms that only one bag remains. Existing sleep, fuel, lighting and other regression checks also pass.
- Model geometry and texture work remains a separate outstanding task pending the user's texture files. These UI checks use the base NeoForge client/server; the full external modpack was not rerun.


## Finished saddlebag textures — 0.1.7 (2026-11-21)

Saddlebags and the crafting half now use a custom 128×128 atlas: stitched chestnut leather, dark leather straps, brass buckles/lantern peg and canvas lining. All 13 permanent cubes and all six faces per cube are textured without dye tint. The original model geometry, group pivots and lantern attachment remain matched against Donkey.java. The optional sleeping bag still uses its separate color-tinted model and slot.

- **88 server tests passed**, including failed dye crafting, removal from the dyeable tag, and migrating old dyed bags without losing the item stored in slot 27. Full build and shader API checks passed.
- **118 graphical client smoke checks passed**, covering the fixed finish, custom atlas on every face, removal of the dye handler, all seven crafting-half cubes, mounted synchronization, sleeping-bag colors and the redesigned UI. An existing 0.1.6 test world loaded successfully after the dye recipe was removed.
- Independent model/resource check verifies unchanged geometry/pivots, untinted full/half models, six mapped faces, in-bounds UV coordinates and quarter-turn face rotations. The atlas is a power-of-two texture; the client reported no missing saddlebag textures or mipmap reduction.
- Native screenshots inspected the equipped leather/stitching/brass finish and inventory models. `models/Saddlebag-textured.bbmodel` contains the finished mappings and an embedded copy of the atlas for editing. The supplied original remains available as the reference.

Textures were authored for this update. The full external modpack and shaders were not rerun; client verification used base NeoForge.


## Administrator debug-stick burnout — 0.1.8 (2026-11-24)

Creative-mode operators can select a normal lantern’s existing `lit` property with the debug stick. A native edit to `lit=false` empties its real saved fuel; `lit=true` restores fuel if empty. Crouching edits reach the debug stick rather than being intercepted by the normal lantern toggle. Native creative/operator permission checks remain authoritative. Ordinary switching off still preserves fuel, and soul/third-party lanterns retain their existing behavior.

The integration test also exposed a pre-existing NeoForge placement issue: the deferred `onPlace` call could overwrite a placed light item’s saved fuel. Fuel is now applied after the placement commits, while direct BlockItem placement remains supported. Cancelled placements do not consume items or create stray fuel records. Cancelled debug-stick edits also leave both the block and its fuel intact.

- Full build: **93 server GameTests passed**, zero failures; shader API detection checks passed.
- New tests exercise actual debug-stick left/right clicks, standing/hanging/waterlogged/Glow Oil lanterns, persisted empty fuel, permissions, crouching, ordinary toggling, native pickaxe drops, normal item-use replacement, oil refueling and bottle return, partial fuel, cancelled placements, soul lights and subclass lantern fixtures.
- Release JAR inspection verifies current mixin registration/resources, valid JSON, and exclusion of GameTest classes and third-party JARs.
- This server-side change was checked through the native item-use integration path. The graphical client and full external modpack were not rerun.


## Matching attunement particles for every player — 0.1.9 (2026-11-24)

All nine trophy attunements now emit a player aura at the same doubled Enderman rate: one batch of two particles every 20 game ticks. Enderman keeps its purple portal particles; Zombie, Skeleton, Creeper, Bogged and Piglin use their matching red, white/light-gray, green, poison-green and gold dust. Wither Skeleton uses dark smoke, Stray snowflakes and Player enchantment particles. Every aura uses the original Enderman spread and speed around the player. Particle options are cached per trophy rather than allocated every tick.

- Full build and shader API checks passed: **95 server GameTests**, zero failures.
- New integration tests inspect actual particle packets delivered to both the attuned player and a nearby observer for every trophy over 40 ticks. They verify two batches of two particles, exact particle types/dust colors, origin, spread, speed, normal client limits and nearby range. Enderman sends a single doubled batch rather than retaining a second old emission.
- Switching attunement changes the emitted type. Clearing attunement or death stops new emissions; a status indicator alone does not create an aura. The tests exercise the server ledger without requiring a loaded trophy block.
- Release JAR inspection checks current resources/version, valid JSON and exclusion of GameTest classes and bundled third-party JARs. The graphical client and external modpack were not rerun; all player particle types use vanilla client providers.


## Administrator trophy placement bypass — 0.1.10 (2026-11-24)

Operators at permission level 2 or higher bypass the one-placed-trophy-per-type check in survival and creative. Creative mode alone grants no exception. Ownership, destruction without drops and one active trophy effect remain in effect. Placement reservations now track counts per owner/type, persist across reloads and read legacy boolean reservations as a count of one. Removing one duplicate preserves the reservation and attunement until the final copy is removed.

Full build and shader API checks passed: **97 server GameTests**, zero failures. New coverage checks survival operator duplicates, creative non-operator rejection, owner identity, single attunement, destruction order, duplicate count persistence and legacy save loading. The previous matching player-particle update is included. Graphical client and full external modpack checks were not rerun for this server-side permission change.


## Mount lantern physics and horseshoes — 0.1.11 (2026-11-24)

Saddlebag lanterns now use the full vanilla model size, compensating for vanilla donkey/mule renderer scale, on a longer chain. A client-tick damped pendulum responds to position acceleration, braking, body turns, gait and vertical motion. Render frames only interpolate; pausing freezes the simulation, teleports reset it, removal clears it and weak entity keys/world-change cleanup avoid retaining old mounts. Swing limits preserve the outward/rearward hanging pose.

Horses no longer expose a lantern slot. Old equipped horse lanterns are returned once on server entity load with fuel/components preserved. Donkeys and mules have five dedicated equipment slots, including horseshoes without requiring saddlebags. Their speed, fall reduction and Gold jump-height bonuses are half the horse values; Gold adds five percent actual jump height. All three mount types show material-colored hoof overlays following adult/foal leg animation, and removing the shoe restores the original appearance.

Validation: **99 server GameTests passed**, shader compatibility checks passed, and the pure Java pendulum checks passed (acceleration, turning, damping, interpolation, bounds and teleport reset). New server checks cover horse slot rejection, lossless/idempotent old-lantern return, donkey/mule speed and actual jump height, fall reduction, synchronization and bonus removal. **124 graphical client smoke checks passed**, covering all five hoof material changes and removal, horse UI without lanterns, and the expanded donkey/mule equipment UI. Native screenshots inspected Gold hooves and a full-size lantern; a controlled movement was recorded to inspect settling. The full external modpack and shaders were not rerun.


## Mounted lantern size correction — 0.1.12 (2026-11-24)

Reduced the saddlebag lantern model to 0.6 of its local size and removed the renderer-scale enlargement. This is approximately half the world-space size of 0.1.11 (52% on donkeys, 55% on mules). The chain is shortened from 0.42 to 0.34 body-local units; the lantern's handle remains attached to the chain and the movement-driven pendulum is retained.

Assembly, pendulum physics checks and independent saddlebag model checks passed. This is a visual size/attachment change; server gameplay checks were not repeated. The original saddlebag geometry, horse slot removal, hoof colors and donkey/mule horseshoe bonuses remain part of this build.

The graphical client passed **124 smoke checks** for 0.1.12, and a native screenshot from the same camera position verified the smaller lantern and shorter chain. Pendulum physics checks passed.

Added bounded vertical tether lift for both jumping and falling, with a landing bounce and damped settling. The rendered chain shortens with the lift to keep the handle attached. Physics checks additionally cover upward motion in both directions, interpolation, settling and teleport reset.

The final vertical-bob addition was verified with the physics checks; the client suite was not repeated for that addition.


## Shorter mounted chain — 0.1.13 (2026-11-24)

Reduced the chain from 0.34 to 0.22 body-local units (about 35% shorter). The lantern remains attached at the chain end, with the compact lantern scale, swing and jump/fall lift preserved. The minimum chain length during maximum lift remains positive (0.08). Updated the model generator as well as the generated attachment constants. Assembly and pendulum checks passed; the graphical client and server gameplay suites were not repeated for this length adjustment.


## Resting lantern contact — 0.1.14 (2026-11-24)

Reduced the fixed rearward lean from 20 degrees to 5 degrees so the resting lantern contacts the bags at the animal's side instead of floating behind the rear corner. The outward lean, compact scale, short chain and motion simulation remain. Donkeys and mules use the same body-local attachment, so their renderer scaling preserves contact. A geometric physics check verifies that the resting lantern's nearest upper corner touches the saddlebag bounds with only minimal overlap.

Full build: **99 server integration tests**, pendulum/contact checks and shader API checks passed. This release includes the previously completed player trophy auras, admin trophy placement bypass, mount slots/bonuses, hoof colors and lantern motion refinements.

The 0.1.14 graphical client passed **124 smoke checks**, and a native screenshot verified contact in the resting pose.


## Centered mounting block and rearward lantern — 0.1.15 (2026-11-24)

Centered the chain on the original brass mounting block: body-local X moves from 0.5 to 0.40625, preserving Y=-0.40625 and Z=0.34375. The generator derives this from the marked cube's center. Changed the resting lean to 5 degrees outward and 15 degrees rearward so the lantern hangs primarily off the bag's rear. Lantern size, chain length and motion simulation are retained. Signed lateral swing is preserved when it crosses the reduced outward resting angle.

Assembly and physics/contact checks passed, including the mounting block center and contact with the rear saddlebag face. Independent geometry checks confirm the supplied model itself is unchanged. This is a visual attachment change; the server gameplay suite was not rerun.

Movement forces and vertical lift now use a 1.15 response multiplier, with the existing safety bounds unchanged. Forward acceleration is explicitly projected onto the mount's facing direction. Physics checks verify the 15% impulse increase and forward acceleration/braking at all four cardinal headings, alongside interpolation, damping, turning, jump/fall lift, teleport reset and attachment contact. Assembly and the independent original-model check passed.

The updated 0.1.15 graphical client passed **124 smoke checks**. A local test-world datapack drives straight movement and stopping for the native motion preview; it is excluded from the release.


## Sustained mounted lantern trailing — 0.1.16 (2026-11-24)

Forward velocity now sets a persistent rearward spring target: 60 degrees per block/tick, with the existing 1.15 movement-response multiplier and a 24-degree target cap. This is additional lean relative to the unchanged resting attachment. The lantern stays trailing at steady forward speed rather than depending solely on acceleration and gait impulses. Slowing reduces the target; stopping returns it to zero. Existing angular safety bounds, interpolation, inertia, jump/fall lift, mounting location and size are preserved.

Assembly and lantern physics checks passed. Tests cover continuous running throughout the gait cycle, lower-speed trailing and settling after stopping at all four cardinal headings, plus existing acceleration, braking, turning, interpolation, bounds, teleport and rest-contact checks. The graphical client and server integration suites were not repeated for this client-only physics adjustment.


## Shaded equipped horseshoes — 0.1.17 (2026-11-24)

Gold tint changes from #FFDB5C to #E6C04A for both the item sprite and mounted overlay. All five equipped horseshoe materials now use Minecraft's iron-block metal texture instead of white concrete, with adjoining highlighted upper rims, material-colored bodies and darker soles. The bands follow the existing animated legs and preserve the adult/foal silhouette for horses, donkeys and mules. The mesh is baked once per renderer and all bands share a texture/render buffer. Equipment rules and bonuses are unchanged.

Assembly, lantern physics checks (including the pending sustained forward trailing change) and **124 graphical client smoke checks** passed. Inspected the gold-equipped horse preview in game. Server gameplay tests were not repeated for this visual change.


## Lower horse horseshoe bands — 0.1.18 (2026-11-24)

Reduced the equipped horseshoe band height by 35% on horses, including foals. Vertical scaling is anchored at the existing hoof bottom so the shoes cover less of the upper hoof while the soles stay in place. All five materials retain the metal texture and rim/sole shading. Donkey and mule band dimensions are unchanged.

Assembly passed. The graphical client and gameplay suites were not rerun for this rendering-only height adjustment.


## Approved saddlebag texture — 0.1.19 (2026-11-24)

Applied the approved mockup's dark chocolate leather, copper-orange trim and brass hardware palette to the existing four-quadrant material atlas. Resampled the generated texture to the existing 128x128 pixel format and regenerated the editable Blockbench copy with the identical embedded atlas. The original UV coordinates, model geometry, pivots, lantern attachment, separate sleeping-bag appearance and non-dyeable behavior are preserved.

Assembly and the independent model/UV check passed: all 14 original cubes and pivots are preserved, and both the full saddlebag and crafting half retain untinted, in-bounds material mappings.

The graphical client passed **124 smoke checks**. Inspected an actual equipped-donkey screenshot with the new atlas. The editable model embeds the exact runtime PNG. Server gameplay tests were not repeated for this texture-only change.


## Saddle-matched leather and silver fittings — 0.1.20 (2026-11-24)

Retextured the saddlebag to warm saddle-brown leather with subtly lighter upper faces and darker lower faces. Removed all yellow stitching and copper-orange trim. Both bag buckles and the lantern mounting block share a medium-silver metal tile. Original mesh, pivots, attachment and non-dyeable behavior are preserved.

Changed the material atlas from 128x128 to 16x16 and mapped each source face at exactly one texture pixel per model unit, including tiny straps, buckles and peg. Upper faces sample the lighter part of their material; lower faces sample darker regions. Updated the full bag, equipped bag, crafting half and embedded Blockbench texture together. The optional sleeping bag retains its separate color-specific texture.

Assembly and the independent original-model/UV check passed. A per-face density check verified one texel per model unit for every bag, strap, buckle and peg face; the editable model embeds the exact runtime PNG.

The final graphical client passed **124 smoke checks**. Inspected the actual equipped-donkey preview: silver buckle and mount, no yellow stitching, saddle-brown shading and vanilla-density pixels. Server gameplay tests were not repeated for this visual change.
