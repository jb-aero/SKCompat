/*
 * CHWorldEdit, MethodScript functions for interacting with WorldEdit
 * Copyright (C) Various contributors over the years.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.jbaero.skcompat;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;

import java.io.File;
import java.lang.reflect.Field;

public class CHWorldEdit {

	public static String docs() {
		return "Provides various methods for hooking into WorldEdit.";
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class sk_pos1 extends SKCompat.SKFunction {

		@Override
		public String getName() {
			return "sk_pos1";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public String docs() {
			return "mixed {[player], array | [player] | array} Sets the player's point 2 to the given location array."
					+ " If the array is null, the point will be cleared."
					+ " If no array is given, current point 2 of the player will be returned as an array in format"
					+ " array(0:xVal, 1:yVal, 2:zVal, x:xVal, y:yVal, z:zVal) or null when the point has not been set."
					+ " In case " + this.getName() + "(null) is called, the argument will be treated as player.";
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return sk_posX_exec(t, env, true, this, args);
		}
	}
	
	@api(environments = CommandHelperEnvironment.class)
	public static class sk_pos2 extends SKCompat.SKFunction {

		@Override
		public String getName() {
			return "sk_pos2";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1, 2};
		}

		@Override
		public String docs() {
			return "mixed {[player], array | [player] | array} Sets the player's point 2 to the given location array."
					+ " If the array is null, the point will be cleared."
					+ " If no array is given, current point 2 of the player will be returned as an array in format"
					+ " array(0:xVal, 1:yVal, 2:zVal, x:xVal, y:yVal, z:zVal) or null when the point has not been set."
					+ " In case " + this.getName() + "(null) is called, the argument will be treated as player.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return sk_posX_exec(t, env, false, this, args);
		}
	}
	
	private static Mixed sk_posX_exec(Target t, Environment env, boolean primary, 
				AbstractFunction caller, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
		MCCommandSender m = null;
		Mixed rawPos = null;
		if (args.length == 2) { // If sk_posX(player, locationArray).
			m = SKWorldEdit.GetPlayer(args[0], t);
			rawPos = args[1];
		} else if (args.length == 1) {
			if (args[0] instanceof CArray) { // If sk_posX(locationArray).
				rawPos = args[0];
			} else { // If sk_posX(player). sk_posX(null) ends up here too, this is desired since player "null" exists.
				m = SKWorldEdit.GetPlayer(args[0], t);
			}
		}
		if (m == null) {
			m = env.getEnv(CommandHelperEnvironment.class).GetPlayer(); // Get the command sender (MCPlayer).
		}
		
		SKCommandSender user = SKWorldEdit.GetSKPlayer(m, t);
		
		if (rawPos != null) {
			
			// Set the new point.
			if(rawPos instanceof CNull) { // Delete a position.

				// Get the region selector.
				RegionSelector sel = user.getLocalSession().getRegionSelector(user.getWorld());
				if (!(sel instanceof CuboidRegionSelector)) {
					// If this happens and the selection of the user was not in the same world as the user, his/her
					// selection will be erased by the "getRegionSelector(user.getWorld())" call.
					throw new CREPluginInternalException("Only cuboid regions are supported with " + caller.getName(), t);
				}
				
				// Get the primary and secondary positions and return if this call doesn't change the selection.
				BlockVector3 pos1 = getPos1((CuboidRegionSelector) sel, t);
				if(primary && pos1 == null) {
					return CVoid.VOID; // Selection did not change.
				}
				BlockVector3 pos2 = getPos2((CuboidRegionSelector) sel, t);
				if(!primary && pos2 == null) {
					return CVoid.VOID; // Selection did not change.
				}
				
				// Clear the selection.
				sel.clear();
				if(primary) {
					pos1 = null;
				} else {
					pos2 = null;
				}
				
				// Re-set the other position if it was set before.
				if(pos1 != null) {
					sel.selectPrimary(pos1, null);
				}
				if(pos2 != null) {
					sel.selectSecondary(pos2, null);
				}
				
				// Update WorldEdit CUI.
				if (m instanceof MCPlayer) {
					try {
						user.getLocalSession().dispatchCUISelection(user);
					} catch (NoSuchMethodError err) {
						// Probably WorldEdit 7.0.x
						ReflectionUtils.invokeMethod(LocalSession.class, user.getLocalSession(), "dispatchCUISelection",
								new Class[]{Player.class}, new Object[]{user});
					}
				}
				
			} else { // Set a position.
				
				BlockVector3 blockPos;
				if(user instanceof SKConsole) {
					MCLocation loc = ObjectGenerator.GetGenerator().location(rawPos, null, t);
					user.setLocation(loc);
					blockPos = BlockVector3.at(Math.floor(loc.getX()), Math.floor(loc.getY()), Math.floor(loc.getZ()));
				} else {
					Vector3D v = ObjectGenerator.GetGenerator().vector(rawPos, t);
					// Floor to int (CUI would accept doubles and select half blocks).
					blockPos = BlockVector3.at(Math.floor(v.X()), Math.floor(v.Y()), Math.floor(v.Z()));
				}

				// Get the region selector.
				RegionSelector sel = user.getLocalSession().getRegionSelector(user.getWorld());
				if (!(sel instanceof CuboidRegionSelector)) {
					// If this happens and the selection of the user was not in the same world as the user, his/her
					// selection will be erased by the "getRegionSelector(user.getWorld())" call.
					throw new CREPluginInternalException("Only cuboid regions are supported with " + caller.getName(), t);
				}
				
				if (primary) {
					sel.selectPrimary(blockPos, null);
				} else {
					sel.selectSecondary(blockPos, null);
				}
				
				// Update WorldEdit CUI.
				if (m instanceof MCPlayer) {
					try {
						sel.explainRegionAdjust(user, user.getLocalSession());
					} catch (NoSuchMethodError err) {
						// Probably WorldEdit 7.0.x
						ReflectionUtils.invokeMethod(CuboidRegionSelector.class, sel, "explainRegionAdjust",
								new Class[]{Player.class, LocalSession.class}, new Object[]{user, user.getLocalSession()});
					}
				}
			}
			
			// Return void as a new point has been selected.
			return CVoid.VOID;
			
		} else {
			
			// When getting the region selector. Don't call "getRegionSelector(user.getWorld())" before checking the
			// world since it will change the world and clear the users selection if the user is in another world.
			
			// Return null if the user does not have a selection in this world.
			if (!user.getWorld().equals(user.getLocalSession().getSelectionWorld())) {
				return CNull.NULL;
			}
			
			// Get the region selector.
			RegionSelector sel = user.getLocalSession().getRegionSelector(user.getWorld());
			
			// Check if the selector region type is supported.
			if (!(sel instanceof CuboidRegionSelector)) {
				throw new CREPluginInternalException("Only cuboid regions are supported with " + caller.getName(), t);
			}
			
			// Get the position.
			BlockVector3 pos = (primary ? getPos1((CuboidRegionSelector) sel, t) : getPos2((CuboidRegionSelector) sel, t));
			
			// Return the position converted to a CArray or CNull if no position is set.
			return (pos == null ? CNull.NULL : ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(pos)));
			
		}
	}
	
	private static BlockVector3 getPos1(CuboidRegionSelector selector, Target t) {
		try {
			return selector.getPrimaryPosition();
		} catch (Exception e) {
			return null; // The primary position is null.
		}
	}
	
	private static BlockVector3 getPos2(CuboidRegionSelector selector, Target t) {
		
		// Return the secondary position from a complete selection if it is available.
		try {
			return selector.getRegion().getPos2();
		} catch (Exception e) {
			// Region is incomplete. There is no way to know if position 2 is set without using reflection.
		}
		
		// Get the secondary position using reflection. This is necessary because there is no way to
		// obtain the secondary position from CuboidRegionSelector. Getting it from the incomplete selection
		// might return an outdated value which is not properly cleared by CuboidRegionSelector.clear().
		BlockVector3 position2;
		try {
			Field position2Field = CuboidRegionSelector.class.getDeclaredField("position2");
			position2Field.setAccessible(true);
			position2 = (BlockVector3) position2Field.get(selector);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			throw new CREPluginInternalException("Getter for secondary position in CommandHelper extension "
					+ PomData.NAME + " version " + PomData.VERSION + " is not compatible with WorldEdit version "
					+ WorldEdit.getVersion(), t);
		}
		
		// Return the secondary position (null if the position is not set).
		return position2;
	}
	
//	public static class sk_points extends SKFunction {
//
//		public String getName() {
//			return "sk_points";
//		}
//
//		public Integer[] numArgs() {
//			return new Integer[]{0, 1, 2};
//		}
//
//		public String docs() {
//			return "mixed {[player], arrayOfArrays | [player]} Sets a series of points, or returns the poly selection for this player, if one is specified."
//					+ " The array should be an array of arrays, and the arrays should be array(x, y, z)";
//		}
//
//		public Class<? extends CREThrowable>[] thrown() {
//			return new Class[]{CREPlayerOfflineException.class, CRECastException.class};
//		}
//
//		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
//			Static.checkPlugin("WorldEdit", t);
//			return CVoid.VOID;
//		}
//	}

	@api(environments = CommandHelperEnvironment.class)
	public static class sk_setblock extends SKCompat.SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidPluginException.class, CREPluginInternalException.class,
					CREPlayerOfflineException.class, CREFormatException.class, CRECastException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender;
			Mixed pat;
			if (args.length == 2) {
				sender = SKWorldEdit.GetPlayer(args[0], t);
				pat = args[1];
			} else {
				sender = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				pat = args[0];
			}
			SKCommandSender user = SKWorldEdit.GetSKPlayer(sender, t);

			SKPattern pattern = new SKPattern();
			if (pat instanceof CArray) {
				pattern.generateBlockPattern((CArray) pat, user, t);
			} else if(pat instanceof CString) {
				pattern.generateBlockPattern((CString) pat, user, t);
			} else {
				throw new CREFormatException("Invalid block pattern.", t);
			}

			EditSession editSession = user.getEditSession(false);
			try {
				editSession.setBlocks(user.getLocalSession().getSelection(user.getWorld()), pattern.getHandle());
			} catch (Exception wee) {
				throw new CREPluginInternalException(wee.getMessage(), t);
			} finally {
				editSession.flushSession();
			}

			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "sk_setblock";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {[player], pattern} Sets the user's selection to blocks defined by the provided pattern."
					+ " The pattern can be a string in the format given to worldedit commands, or it can be"
					+ " a normal array of associative arrays. If the array is empty, the entire selection will be"
					+ " set to air. The inner arrays consist of a required 'block' field describing the block's"
					+ " material and properties, and an optional decimal 'weight' field."
					+ " If weight is not given it defaults to 1. The weight represents that block's chance"
					+ " of being selected for the next random block setting.";
		}
	}

	@api
	public static class skcb_copy extends SKCompat.SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class, CREPlayerOfflineException.class, CREIOException.class,
					CREInvalidPluginException.class, CRELengthException.class, CREFormatException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean entities = false;
			boolean biomes = false;
			MCCommandSender sender = null;
			MCLocation loc = null;
			if(args[0] instanceof CArray) {
				loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			} else {
				sender = SKWorldEdit.GetPlayer(args[0], t);
			}

			if(args.length == 2) {
				CArray options = Static.getArray(args[1], t);
				if (options.containsKey("entities")) {
					entities = ArgumentValidation.getBooleanObject(options.get("entities", t), t);
				}
				if (options.containsKey("biomes")) {
					biomes = ArgumentValidation.getBooleanObject(options.get("biomes", t), t);
				}
			}

			SKClipboard.Copy(sender, loc, entities, biomes, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "skcb_copy";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {location | player, [options]} Copies the selected region into the clipboard."
					+ " If a location is specified it will use the console's clipboard"
					+ " and the location will be used as the origin point for the clipboard."
					+ " If ~console is explicitly specified instead, it will use the last set position as the origin."
					+ " An associative array of options can be provided, all of which default to false."
					+ " If 'entities' is true, entities within the schematic will be pasted."
					+ " If 'biomes' is true, the biomes within the schematic with be pasted.";
		}
	}

	@api
	public static class skcb_load extends SKCompat.SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class, CREPlayerOfflineException.class, CREIOException.class,
					CREInvalidPluginException.class, CRELengthException.class, CREFormatException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String filename = args[0].val();
			MCCommandSender sender = null;
			if (args.length == 2) {
				sender = SKWorldEdit.GetPlayer(args[1], t);
			}
			SKClipboard.Load(sender, filename, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "skcb_load";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {filename, [player]} Loads a schematic into the clipboard from file."
					+ " It will use the directory specified in WorldEdit's config."
					+ " By default it will use the console's clipboard, but will use a player's if specified.";
		}
	}

	@api
	public static class skcb_save extends SKCompat.SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class, CREPlayerOfflineException.class, CREIOException.class,
					CREInvalidPluginException.class, CRELengthException.class, CREFormatException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String filename = args[0].val();
			MCCommandSender sender = null;
			if(args.length > 1) {
				sender = SKWorldEdit.GetPlayer(args[1], t);
			}
			SKClipboard.Save(sender, filename, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "skcb_save";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {filename, [player]} Saves a schematic in the clipboard to file."
					+ " It will use the directory specified in WorldEdit's config."
					+ " By default it will use the console's clipboard, but will use a player's if specified.";
		}
	}

	@api
	public static class skcb_rotate extends SKCompat.SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidPluginException.class, CRENotFoundException.class,
					CREPlayerOfflineException.class, CRERangeException.class, CRECastException.class,
					CREIllegalArgumentException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int yaxis = 0,
					xaxis = 0,
					zaxis = 0;
			MCCommandSender sender = null;
			switch (args.length) {
				case 3:
					xaxis = Static.getInt32(args[1], t);
					zaxis = Static.getInt32(args[2], t);
				case 1:
					yaxis = Static.getInt32(args[0], t);
					break;
				case 4:
					xaxis = Static.getInt32(args[2], t);
					zaxis = Static.getInt32(args[3], t);
				case 2:
					yaxis = Static.getInt32(args[1], t);
					sender = SKWorldEdit.GetPlayer(args[0], t);
					break;
			}

			if (yaxis % 90 != 0 || xaxis % 90 != 0 || zaxis % 90 != 0) {
				throw new CREIllegalArgumentException("Axes must be multiples of 90 degrees.", t);
			}

			SKClipboard.Rotate(sender, xaxis, yaxis, zaxis, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "skcb_rotate";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		@Override
		public String docs() {
			return "void {[player,] int y-axis, [int x-axis, int z-axis]}"
					+ " Rotates the clipboard by the given (multiple of 90) degrees for each corresponding axis."
					+ " To skip an axis, simply give it a value of 0. If a player is supplied, theirs will be rotated,"
					+ " otherwise the console will be used.";
		}
	}

	@api
	public static class skcb_paste extends SKCompat.SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class, CRERangeException.class,
					CRENotFoundException.class, CRECastException.class, CREInvalidPluginException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean airless = false,
					fastMode = false,
					origin = false,
					select = false,
					entities = false,
					biomes = false;
			MCCommandSender sender = null;
			if (args[0] instanceof CArray) {
				SKWorldEdit.GetSKPlayer(null, t).setLocation(ObjectGenerator.GetGenerator().location(args[0], null, t));
			} else {
				sender = SKWorldEdit.GetPlayer(args[0], t);
			}
			if (args.length >= 2) {
				CArray options = Static.getArray(args[1], t);
				if (options.containsKey("airless")) {
					airless = ArgumentValidation.getBooleanObject(options.get("airless", t), t);
				}
				if (options.containsKey("fastmode")) {
					fastMode = ArgumentValidation.getBooleanObject(options.get("fastmode", t), t);
				}
				if (options.containsKey("origin")) {
					origin = ArgumentValidation.getBooleanObject(options.get("origin", t), t);
				}
				if (options.containsKey("select")) {
					select = ArgumentValidation.getBooleanObject(options.get("select", t), t);
				}
				if (options.containsKey("entities")) {
					entities = ArgumentValidation.getBooleanObject(options.get("entities", t), t);
				}
				if (options.containsKey("biomes")) {
					biomes = ArgumentValidation.getBooleanObject(options.get("biomes", t), t);
				}
			}
			SKClipboard.Paste(sender, airless, biomes, entities, fastMode, origin, select, t);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "skcb_paste";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "void {location | player, [options]}"
					+ " Pastes a schematic from the player's clipboard if a player is provided,"
					+ " or from the console's clipboard if a location is given, as if a player was standing there."
					+ " An associative array of options can be provided, all of which default to false."
					+ " If 'airless' is true, air blocks from the schematic will not replace blocks in the world."
					+ " If 'fastmode' is true, the function will use WorldEdit's 'fastmode' to paste."
					+ " If 'origin' is true, the schematic will be pasted at the original location it was copied from."
					+ " If 'select' is true, the pasted blocks will be automatically selected."
					+ " If 'entities' is true, entities within the schematic will be pasted."
					+ " If 'biomes' is true, the biomes within the schematic with be pasted.";
		}
	}

	@api
	public static class skcb_clear extends SKCompat.SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CRECastException.class, CREInvalidPluginException.class,
					CRELengthException.class, CREFormatException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCCommandSender sender = null;
			if(args.length == 1) {
				sender = SKWorldEdit.GetPlayer(args[0], t);
			}
			SKCommandSender user = SKWorldEdit.GetSKPlayer(sender, t);
			user.getLocalSession().setClipboard(null);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "skcb_clear";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "void {[player]} Clears the clipboard for the specified player or console.";
		}
	}

	@api
	public static class sk_schematic_exists extends SKCompat.SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CRECastException.class, CREInvalidPluginException.class,
					CREIOException.class, CREFormatException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			WorldEdit worldEdit = WorldEdit.getInstance();
			String filename = args[0].val();
			File dir = worldEdit.getWorkingDirectoryFile(worldEdit.getConfiguration().saveDir);
			File f;
			try {
				try {
					f = worldEdit.getSafeOpenFile(null, dir, filename, "schem");
				} catch (NoSuchMethodError err) {
					// Probably WorldEdit 7.0.x
					f = (File) ReflectionUtils.invokeMethod(WorldEdit.class, worldEdit, "getSafeOpenFile",
							new Class[]{Player.class, File.class, String.class, String.class, String[].class},
							new Object[]{null, dir, filename, "schem", null});
				}
			} catch (Exception fne) {
				throw new CREFormatException(fne.getMessage(), t);
			}

			return CBoolean.get(f.exists());
		}

		@Override
		public String getName() {
			return "sk_schematic_exists";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {filename} Returns whether a schematic by that name exists."
					+ " It will use the directory specified in WorldEdit's config."
					+ " If an extension is not provided, 'filename.schem' will be checked."
					+ " Ignores legacy schematics unless the legacy extension is explicitly used."
					+ " Throws FormatException if using an invalid filename.";
		}
	}
	
	@api
	public static class sk_clipboard_info extends SKCompat.SKFunction {
		
		@Override
		public String getName() {
			return "sk_clipboard_info";
		}
		
		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}
		
		@Override
		public String docs() {
			return "array {[player]} Returns an array with selection info of the give players clipboard (or null when the clipboard is empty)."
					+ "The returned array is in format: {origin:{x,y,z}, dimensions:{x,y,z}, minPoints{original:{x,y,z}, relative:{x,y,z}}"
					+ ", maxPoints{original:{x,y,z}, relative:{x,y,z}}}.";
		}
		
		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCCommandSender sender;
			if (args.length == 0) {
				sender = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			} else {
				sender = SKWorldEdit.GetPlayer(args[0], t);
			}
			
			SKCommandSender user = SKWorldEdit.GetSKPlayer(sender, t);

			LocalSession localSession = user.getLocalSession();
			ClipboardHolder clipHolder;
			try {
				clipHolder = localSession.getClipboard();
			} catch (Exception e) {
				return CNull.NULL; // Return null as the given player has an empty clipboard.
			}
			Clipboard clip = clipHolder.getClipboard();
			Transform transform = clipHolder.getTransform();
			
			// Create return array.
			CArray ret = new CArray(t);
			CArray origin            = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(clip.getOrigin()));
			CArray dimensions        = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(clip.getDimensions()));
			CArray minPointOriginal  = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(clip.getMinimumPoint()));
			CArray maxPointOriginal  = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(clip.getMaximumPoint()));
			CArray minPointRelative  = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(clip.getMinimumPoint().subtract(clip.getOrigin())));
			CArray maxPointRelative  = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(clip.getMaximumPoint().subtract(clip.getOrigin())));
			CArray minPoint = new CArray(t);
			CArray maxPoint = new CArray(t);
			minPoint.set("original", minPointOriginal, t); // Original copy region world coords (//paste -o).
			maxPoint.set("original", maxPointOriginal, t); // Original copy region world coords (//paste -o).
			minPoint.set("relative", minPointRelative, t); // Initialize to non-rotated/flipped region.
			maxPoint.set("relative", maxPointRelative, t); // Initialize to non-rotated/flipped region.
			
			ret.set("origin"    , origin    , t);
			ret.set("dimensions", dimensions, t);
			ret.set("minPoint"  , minPoint  , t);
			ret.set("maxPoint"  , maxPoint  , t);
			
			if (!(transform instanceof AffineTransform)) {
				return ret; // Return here, as we can't add any rotation and paste data.
			}
			AffineTransform affineTransform = (AffineTransform) transform;
			
			Vector3 minPointOriginalVec = affineTransform.apply(clip.getMinimumPoint().subtract(clip.getOrigin()).toVector3()).add(clip.getOrigin().toVector3());
			Vector3 maxPointOriginalVec = affineTransform.apply(clip.getMaximumPoint().subtract(clip.getOrigin()).toVector3()).add(clip.getOrigin().toVector3());
			Vector3 minPointRelativeVec = affineTransform.apply(clip.getMinimumPoint().subtract(clip.getOrigin()).toVector3());
			Vector3 maxPointRelativeVec = affineTransform.apply(clip.getMaximumPoint().subtract(clip.getOrigin()).toVector3());
			
			CArray minPointOriginalCVec = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(minPointOriginalVec.toBlockPoint()));
			CArray maxPointOriginalCVec = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(maxPointOriginalVec.toBlockPoint()));
			CArray minPointRelativeCVec = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(minPointRelativeVec.toBlockPoint()));
			CArray maxPointRelativeCVec = ObjectGenerator.GetGenerator().vector(SKWorldEdit.vtov(maxPointRelativeVec.toBlockPoint()));
			
			minPoint.set("original", minPointOriginalCVec, t); // 'Original' copy region world coords (//paste -o) inc rotation & flip.
			maxPoint.set("original", maxPointOriginalCVec, t); // 'Original' copy region world coords (//paste -o) inc rotation & flip.
			minPoint.set("relative", minPointRelativeCVec, t); // Relative copy selection world coords inc rotation & flip (//paste).
			maxPoint.set("relative", maxPointRelativeCVec, t); // Relative copy selection world coords inc rotation & flip (//paste).
			
			return ret;
		}
	}
}
