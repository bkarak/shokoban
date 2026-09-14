# Sokoban

A standalone desktop Sokoban for modern Java, rebuilt from the 1990s-era browser applet
that used to live in this directory. Its source signs itself `Sokoban Ver. 1.00 - Java Port By
ReneGade/TiS`; ReneGade/TiS was the handle of this repository's owner at the time, so the applet
and the rebuild have the same author. The original tiles, skins, logo and levels are preserved;
the game logic, rendering and level parsing were rewritten. Sixty-five levels and three skins
ship with it — see [Levels](#levels).

![Sokoban running on the desktop](docs/screenshot.png)

## Build and run

Requires JDK 17 or newer (built and tested on JDK 26).

```sh
mvn                       # default goal is `package`
java -jar target/sokoban-2.0.0.jar

# or, without packaging
mvn exec:java
```

An optional argument opens a level file directly:

```sh
java -jar target/sokoban-2.0.0.jar my-levels/warehouse.xsb
```

A native macOS bundle can be produced with the JDK's own packager:

```sh
jpackage --name Sokoban --app-version 2.0.0 --type app-image \
         --input target --main-jar sokoban-2.0.0.jar
```

## Controls

| Action | Keys |
| --- | --- |
| Move | arrow keys, `W` `A` `S` `D`, or `I` `J` `K` `L` |
| Undo | `U`, `Backspace`, or ⌘/Ctrl-`Z` |
| Restart level | ⌘/Ctrl-`R` |
| Next level | ⌘/Ctrl-`]` |
| Open level file | ⌘/Ctrl-`O` |

Levels and skins can also be switched from the toolbar pickers. Undo history is unlimited.

## Level formats

Two notations are accepted. The legacy applet format, keyed fields separated by `%%` with map
rows separated by `;;`:

```
Name=levelOne %% Author=default %% Width=10 %% Height=10 %%
Data=
1 1 1 1 1 1 1 1 1 1 ;;
1 2 3 3 3 3 3 3 5 1 ;;
...
```

where `1` is wall, `2` goal, `3` floor, `4` box, `5` player, `6` player on goal, `7` box on goal.
`Width` and `Height` are now derived from the data, so they cannot disagree with the map.

And the standard `.xsb` text format used by most Sokoban collections:

```
; Title: tiny
; Author: tester
#####
#@$.#
#####
```

Anything outside the grid is treated as a wall, which keeps the borderless maps
(`pipa`, `andrew2`) playable rather than throwing.

## Levels

Sixty-five levels ship in the jar. Five are the applet's own — `levelOne`, `level2`, `andrew1`,
`andrew2` and `pipa`, in the legacy `%%` notation. The other sixty are **LOMA** (Levels Of Multi
Authors), a collection assembled by Aymeric du Peloux and distributed with the SoKonvert utility
that sat next to the applet on the website; they are stored as `.xsb` under
`src/main/resources/sokoban/maps/loma/`.

The LOMA files here are **not** the ones the applet served. The site's copies came out of
SoKonvert, which silently mapped `*` — a box already standing on a goal — to plain floor. That
cost thirty-six of the fifty-nine published levels both a box and a goal apiece, which kept the
box count matching the goal count and so looked well-formed while being unplayable: LOMA03-01
arrived with one box instead of three and no way to reach the goal at all. These sixty are read
straight from `levtext.txt`, the collection's own text, which is where SoKonvert read them from
too. The sixtieth, LOMA10-06, is in that file but never made it through the converter, so the
website never had it.

Every bundled level has been solved by breadth-first search over the real `GameState` rules,
so all sixty-five are known to be winnable.

## Artwork

The tiles were drawn at 20×20 and the UI artwork at 96×37, which looks blocky on a large
window and soft on a HiDPI screen. `tools/AssetUpscaler.java` upscales them offline:

```sh
java tools/AssetUpscaler.java        # rewrites the PNGs under src/main/resources/sokoban
```

Tiles become 80×80 PNGs and the UI artwork gains a `@2x` variant. Both use EPX (Scale2x),
which doubles pixels and only bends an output pixel towards a neighbour where two adjacent
neighbours agree — that rounds off the diagonals on the sprites and keeps the pixel-font
lettering sharp. Bicubic resampling is available (`--tiles smooth`, `--chrome smooth`) but
only softens this kind of art. Tile neighbours are sampled toroidally, so the upscales still
repeat seamlessly across the board.

The originals stay in place as the input and as a fallback: `Skin` prefers `<tile>.png` and
drops back to `<tile>.gif`, and `Resources.readArtwork` pairs each GIF with its `@2x` PNG in a
`BaseMultiResolutionImage` so Swing picks the sharper bitmap on HiDPI displays without
changing any logical sizes. The board snaps tiles to whole multiples of the original 20-pixel
grid, which keeps textured floors seam-free at every zoom step.

## Layout

```
src/main/java/org/bkarak/sokoban/        game model, level parsing, resources
src/main/java/org/bkarak/sokoban/ui/     Swing window and board rendering
src/main/resources/sokoban/              levels, skins, artwork, maps.cfg, skins.cfg
src/main/resources/sokoban/maps/loma/    the LOMA collection, one .xsb per level
src/test/java/                           JUnit 5 tests for the parser, game rules and assets
tools/AssetUpscaler.java                 offline artwork upscaler, not part of the jar
tools/Screenshot.java                    regenerates docs/screenshot.png, not part of the jar
legacy/applet/                           the original applet sources and assets, untouched
```

Bundled levels are listed in `maps.cfg` and skins in `skins.cfg`; adding a line to either file
plus the matching files under `src/main/resources/sokoban/` is enough to extend the game.

## What changed from the applet

- `Applet` and its HTML page are gone; the app is a `JFrame` launched from a `main` method,
  with a menu bar, keyboard accelerators and a resizable, self-scaling board.
- Assets load from the classpath instead of `getCodeBase()` HTTP requests, so the jar is
  self-contained and runs offline.
- Rendering happens inside `paintComponent` from the model state, replacing the old habit of
  drawing through `getGraphics()` outside the paint cycle.
- Raw `Hashtable`, `Vector` and `Stack` gave way to generics, records and enums; the level
  parser reports errors instead of failing silently, and a variable named `enum` no longer
  keeps the code from compiling on anything past Java 1.4.
- Game rules are separated from the UI and covered by tests: pushes, blocked moves, undo,
  restart and the win condition.
