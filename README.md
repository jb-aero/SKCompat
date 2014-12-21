SKCompat
========

An extension to CommandHelper providing access to features of other plugins in the sk89q family.

## Functions
### SKWorldEdit
Provides various methods for hooking into WorldEdit.

#### mixed sk\_pos1([player], locationArray | [player]):
Sets the player's point 1, or returns it if the array to set isn't specified. If the location is returned, it is returned as a 4 index array:(x, y, z, world)

#### mixed sk\_pos2([player], array | [player]):
Sets the player's point 2, or returns it if the array to set isn't specified

#### void skcb\_load(filename):
Loads a schematic into the clipboard from file. It will use the directory specified in WorldEdit's config.

#### void skcb\_paste(location, [array]):
Pastes a schematic from the clipboard as if a player was standing at the location, An associative array of options can be provided, all of which default to false. If 'airless' is true, air blocks from the schematic will not replace blocks in the world. If 'fastmode' is true, the function will use WorldEdit's 'fastmode' to paste. If 'origin' is true, *TEST*. If 'select' is true, the pasted blocks will be automatically selected. Both ignoreAir and entities default to false.

#### void skcb\_rotate(int y-axis[, int x-axis, int z-axis]):
Rotates the clipboard by the given (multiple of 90) degrees for each corresponding axis. To skip an axis, simply give it a value of 0.

### SKWorldGuard
Provides various methods for hooking into WorldGuard

#### array sk\_all\_regions([world]):
Returns all the regions in all worlds, or just the one world, if specified.

#### boolean sk\_can\_build([player,] locationArray):
Returns whether or not player can build at the location, according to WorldGuard. If player is not given, the current player is used.

#### mixed sk\_current\_regions([player]):
Returns the list regions that player is in. If no player specified, then the current player is used. If region is found, an array of region names are returned, else an empty array is returned

#### mixed sk\_region\_check\_flag(locationArray, flagName, [player]):
Check state of selected flag in defined location. FlagName should be any supported flag from [this list](http://wiki.sk89q.com/wiki/WorldGuard/Regions/Flags). Player defaults to the current player.

#### void sk\_region\_exists([world], name):
Check if a given region exists.

#### array sk\_region\_info(region, world, [value]):
Given a region name, returns an array of information about that region.

If value is set, it should be an integer of one of the following indexes, and only that information for that index will be returned. Otherwise if value is not specified (or is -1), it returns an array of information with the following pieces of information in the specified index:<ul> <li>0 - An array of points that define this region</li> <li>1 - An array of owners of this region</li> <li>2 - An array of members of this region</li> <li>3 - An array of arrays of this region's flags, where each array is: array(flag_name, value)</li> <li>4 - This region's priority</li> <li>5 - The volume of this region (in meters cubed)</li></ul>If the region cannot be found, a PluginInternalException is thrown.

#### array sk\_region\_intersect(world, region1, [array(region2, [regionN...])]):
Returns an array of regions names which intersect with defined region. You can pass an array of regions to verify or omit this parameter and all regions in selected world will be checked.

#### array sk\_region\_members(region, world):
Returns an array of members of this region. If the world or region cannot be found, a PluginInternalException is thrown.

#### boolean sk\_region\_overlaps(world, region1, array(region2, [regionN...])):
Returns true or false whether or not the specified regions overlap.

#### array sk\_region\_owners(region, world):
Returns an array of owners of this region. If the world or region cannot be found, a PluginInternalException is thrown.

#### int sk\_region\_volume(region, world):
Returns the volume of the given region in the given world.

#### mixed sk\_regions\_at(Locationarray):
Returns a list of regions at the specified location. If regions are found, an array of region names are returned, otherwise, an empty array is returned.

