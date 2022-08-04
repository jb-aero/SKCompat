SKCompat
========

An extension to [CommandHelper](https://github.com/EngineHub/CommandHelper) providing access to features of other plugins in the sk89q family. Currently supports WorldEdit and WorldGuard.

Download or compile the correct version for your server:
<br>**[SKCompat 2.1.1](https://github.com/jb-aero/SKCompat/commit/058a9fd2bf812af7b76516d751edf89ce95936c3)** (CommandHelper 3.3.2, Minecraft 1.7.10 - 1.12.2, WorldEdit/WorldGuard 6.x)
<br>**[SKCompat 3.1.4](https://letsbuild.net/jenkins/job/SKCompat/lastSuccessfulBuild/)** (CommandHelper 3.3.4 - 3.3.5, Minecraft 1.13.2 - 1.18.2, WorldEdit 7.2.x, WorldGuard 7.0.x)

NOTE: CHWorldEdit provides functions for WorldEdit. CHWorldGuard provides functions for WorldGuard. SKCompat combines both, which means you only need to download one file.

-> [How to See Documentation In-Game](https://github.com/jb-aero/SKCompat/blob/master/DOCUMENTATION.md)

## Compact Function List
### WorldEdit

In these WorldEdit functions, the player argument can be `null` or `'~console'` to use the console as a user.

**sk_pos1([player], array | [player] | array)** Sets the player's point 1 to the given location array.<br>
**sk_pos2([player], array | [player] | array)** Sets the player's point 2 to the given location array.<br>
**sk_setblock([player], pattern)** Sets the current selection to blocks defined by the provided block pattern.<br>
**sk_replace_blocks([player], mask, pattern)** Replaces blocks matching the mask in the current selection with a block pattern.
**skcb_copy(location | player, [options])** Copies the selected region into the clipboard.<br>
**skcb_paste(location | player, [options])** Pastes a schematic from the player's clipboard.<br>
**skcb_load(filename, [player])** Loads a schematic into the clipboard from file.<br>
**skcb_save(filename, [overwrite], [player])** Saves a schematic in the clipboard to file.<br>
**skcb_rotate([player,] y, [x, z])** Rotates the clipboard by the given degrees for each corresponding axis.<br>
**skcb_clear([player])** Clears the clipboard for the specified player.<br>
**sk_schematic_exists(filename)** Returns whether a schematic by that name exists.<br>
**sk_clipboard_info([player])** Returns an array with selection info of the give player's clipboard.

### WorldGuard

**sk_all_regions([world])** Returns all the regions in all worlds, or just the one world.<br>
**sk_region_info(region, world, [value])** Returns information about the a region in the given world.<br>
**sk_region_overlaps(world, region1, array(region2, [regionN...]))** Returns whether or not the specified regions overlap.<br>
**sk_region_intersect(world, first_region, [other_region(s)]}** Returns array of regions which intersect with first region.<br>
**sk_current_regions([player])** Returns an array of regions a player is in.<br>
**sk_regions_at(locationArray)** Returns a list of regions at the specified location.<br>
**sk_region_volume(region, world)** Returns the volume of a region in the given world.<br>
**sk_region_create([world], name, array(locationArray, [...]))** Create region of the given name in the given world.<br>
**sk_region_update([world], region, array(locationArray, [...]))** Updates the location of a given region to the new location.<br>
**sk_region_rename([world], oldName, newName])** Renames an existing region.<br>
**sk_region_remove([world], region)** Removes existing region.<br>
**sk_region_exists([world], region)** Check if a region by that name exists in a world.<br>
**sk_region_addowner(region, [world], [owner(s)])** Add owner(s) to a region.<br>
**sk_region_remowner(region, [world], [owner(s)])** Removes owner(s) from a region.<br>
**sk_region_owners(region, world)** Returns an array of owners of this region.<br>
**sk_region_addmember(region, [world], [member(s)])** Add member(s) to a region.<br>
**sk_region_remmember(region, [world], [member(s)])** Remove member(s) from a region.<br>
**sk_region_members(region, world)** Returns an array of members of this region.<br>
**sk_register_flag(name, type)** Registers a new flag (on startup only). Type must be BOOLEAN, DOUBLE, INTEGER, or STRING.<br>
**sk_region_flag(world, region, flagName, flagValue, [group])** Add/change/remove flag in a region.<br>
**sk_region_check_flag(locationArray, flagName, [player])** Check state of selected flag in defined location.<br>
**sk_region_flags(region, world)** Returns an associative array with the flags of the region.<br>
**sk_region_setpriority([world], region, priority)** Sets priority for a region.<br>
**sk_region_setparent(world, region, [parentRegion])** Sets parent region for a region.<br>
**sk_can_build([player], locationArray)** Returns whether or not player can build at the location.
