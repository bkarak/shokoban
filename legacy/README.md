# Legacy applet

`applet/` holds the original Sokoban applet exactly as it was before the desktop port:
the AWT sources (`shokobanApplet.java` and friends), `shokobanApplet.html`, the `.cfg`
files, the Kawa project files, and the `images/`, `skins/` and `maps/` asset folders.
The `... 2.java` and `... 3.gif` files are Dropbox conflict copies that came with the
directory and are kept here for completeness.

This is the development directory, not the deployed applet. What actually ran on the website
carried two things this folder does not: a third skin, `xsoko`, and the LOMA level collection
instead of the five maps here. Both were recovered from the website's repository — the skin
verbatim, the levels from the collection's own source rather than from the site's converted
copies, which were broken. The README at the root has the account.

Nothing in this folder is compiled or shipped. The live copies of the artwork, skins and
levels now live in `src/main/resources/sokoban/`.
