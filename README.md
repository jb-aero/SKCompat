# SKCompat

An extension to [CommandHelper](https://github.com/EngineHub/CommandHelper) providing access to functionality in WorldEdit and WorldGuard.

**CHWorldEdit** provides functions for WorldEdit. **CHWorldGuard** provides functions for WorldGuard. **SKCompat** combines both, which means you only need to download one file.
See **[CHRegionChange](https://github.com/PseudoKnight/CHRegionChange)** for a WorldGuard event that fires on region boundary crossings.

## Builds

### Latest

**[SKCompat 3.2.3](https://github.com/jb-aero/SKCompat/releases/tag/v3.2.3)** (CommandHelper 3.3.5, Spigot 1.16.5 - 1.20.4, WorldEdit 7.2 - 7.3, WorldGuard 7.0)

### Legacy

**[SKCompat 3.1.4](https://github.com/jb-aero/SKCompat/releases/tag/v3.1.4)** (CommandHelper 3.3.4 - 3.3.5, Spigot 1.13.2 - 1.19.4, WorldEdit 7.0 - 7.2, WorldGuard 7.0)  
**[SKCompat 2.1.1](https://github.com/jb-aero/SKCompat/releases/tag/v2.1.1)** (CommandHelper 3.3.2, Spigot 1.7.10 - 1.12.2, WorldEdit/WorldGuard 6)

## Documentation

Full function documentation can be seen using VSCode with the MethodScript extension when SKCompat is installed.
Alternatively, [here's a guide](https://github.com/jb-aero/SKCompat/blob/master/DOCUMENTATION.md) to create a command to
see documentation in-game.

### Compact Function Reference
#### WorldEdit

In these WorldEdit functions, user refers to console or player, where console is `null` or `'~console'`.

**sk_pos1([user], array | [user] | array)** Sets or gets the user's point 1.  
**sk_pos2([user], array | [user] | array)** Sets or gets the user's point 2.  
**sk_setblock([user], pattern)** Sets the current selection to blocks defined by the provided block pattern.  
**sk_replace_blocks([user], mask, pattern)** Replaces blocks matching the mask in the current selection with a pattern.  
**skcb_copy(location | user, [options])** Copies the selected region into the clipboard.  
**skcb_paste(location | user, [options])** Pastes a schematic from the user's clipboard.  
**skcb_load(filename, [user])** Loads a schematic into the clipboard from file.  
**skcb_save(filename, [overwrite], [user])** Saves a schematic in the clipboard to file.  
**skcb_rotate([user,] y, [x, z])** Rotates the clipboard by the given degrees for each corresponding axis.  
**skcb_clear([user])** Clears the clipboard for the specified user.  
**sk_schematic_exists(filename)** Returns whether a schematic by that name exists.  
**sk_clipboard_info([user])** Returns an array with selection info of the give user's clipboard.

#### WorldGuard

**sk_all_regions([world])** Returns all the regions in all worlds, or just the one world.  
**sk_region_info(region, world, [value])** Returns information about the a region in the given world.  
**sk_region_overlaps(world, region1, array(region2, [regionN...]))** Returns whether or not the specified regions overlap.  
**sk_region_intersect(world, first_region, [other_region(s)]}** Returns array of regions which intersect with first region.  
**sk_current_regions([player])** Returns an array of regions a player is in.  
**sk_regions_at(locationArray)** Returns a list of regions at the specified location.  
**sk_region_volume(region, world)** Returns the volume of a region in the given world.  
**sk_region_create([world], name, array(locationArray, [...]))** Create region of the given name in the given world.  
**sk_region_update([world], region, array(locationArray, [...]))** Updates the boundaries of a given region.  
**sk_region_rename([world], oldName, newName])** Renames an existing region.  
**sk_region_remove([world], region)** Removes existing region.  
**sk_region_exists([world], region)** Check if a region by that name exists in a world.  
**sk_region_addowner(region, [world], [owner(s)])** Add owner(s) to a region.  
**sk_region_remowner(region, [world], [owner(s)])** Removes owner(s) from a region.  
**sk_region_owners(region, world)** Returns an array of owners of this region.  
**sk_region_addmember(region, [world], [member(s)])** Add member(s) to a region.  
**sk_region_remmember(region, [world], [member(s)])** Remove member(s) from a region.  
**sk_region_members(region, world)** Returns an array of members of this region.  
**sk_register_flag(name, [type])** Registers a new region flag. Type must be BOOLEAN, DOUBLE, INTEGER, LOCATION or STRING (default).  
**sk_region_flag(world, region, flagName, flagValue, [group])** Add/change/remove flag in a region.  
**sk_region_check_flag(locationArray, flagName, [player])** Check state of selected flag in defined location.  
**sk_region_flags(region, world)** Returns an associative array with the flags of the region.  
**sk_region_setpriority([world], region, priority)** Sets priority for a region.  
**sk_region_setparent(world, region, [parentRegion])** Sets parent region for a region.  
**sk_can_build([player], locationArray)** Returns whether or not player can build at the location.
