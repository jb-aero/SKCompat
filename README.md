# SKCompat

An extension to [CommandHelper](https://github.com/EngineHub/CommandHelper) providing access to functionality in WorldEdit and WorldGuard.

**CHWorldEdit** provides functions for WorldEdit. **CHWorldGuard** provides functions for WorldGuard. **SKCompat** combines both, which means you only need to download one file.
See **[CHRegionChange](https://letsbuild.net/jenkins/job/CHRegionChange/)** for a WorldGuard event that fires on region boundary crossings.

## Builds

### Latest

**[SKCompat 3.1.4](https://letsbuild.net/jenkins/job/SKCompat/lastSuccessfulBuild/)** (CommandHelper 3.3.4 - 3.3.5, Spigot 1.13.2 - 1.19.2, WorldEdit 7.2.x, WorldGuard 7.0.x)

### Legacy

If you need a build for an older version of CommandHelper, you will have to compile it yourself at this commit.

**[SKCompat 2.1.1](https://github.com/jb-aero/SKCompat/commit/058a9fd2bf812af7b76516d751edf89ce95936c3)** (CommandHelper 3.3.2, Spigot 1.7.10 - 1.12.2, WorldEdit/WorldGuard 6.x)

## Documentation

Full function documentation can be seen using VSCode with the MethodScript extension when SKCompat is installed.
Alternatively, [here's a guide](https://github.com/jb-aero/SKCompat/blob/master/DOCUMENTATION.md) to create a command to
see documentation in-game.

### Compact Function Reference
#### WorldEdit

In these WorldEdit functions, user refers to console or player, where console is `null` or `'~console'`.

**sk_pos1([user], array | [user] | array)** Sets or gets the user's point 1.<br>
**sk_pos2([user], array | [user] | array)** Sets or gets the user's point 2.<br>
**sk_setblock([user], pattern)** Sets the current selection to blocks defined by the provided block pattern.<br>
**sk_replace_blocks([user], mask, pattern)** Replaces blocks matching the mask in the current selection with a block pattern.<br>
**skcb_copy(location | user, [options])** Copies the selected region into the clipboard.<br>
**skcb_paste(location | user, [options])** Pastes a schematic from the user's clipboard.<br>
**skcb_load(filename, [user])** Loads a schematic into the clipboard from file.<br>
**skcb_save(filename, [overwrite], [user])** Saves a schematic in the clipboard to file.<br>
**skcb_rotate([user,] y, [x, z])** Rotates the clipboard by the given degrees for each corresponding axis.<br>
**skcb_clear([user])** Clears the clipboard for the specified user.<br>
**sk_schematic_exists(filename)** Returns whether a schematic by that name exists.<br>
**sk_clipboard_info([user])** Returns an array with selection info of the give user's clipboard.

#### WorldGuard

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
