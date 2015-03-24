package com.zeoldcraft.skcompat;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MVector3D;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.command.ClipboardCommands;
import com.sk89q.worldedit.command.RegionCommands;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.world.registry.WorldData;
import com.zeoldcraft.skcompat.SKCompat.SKFunction;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

/**
 *
 * @author jb_aero
 */
public class SKWorldEdit {

    public static String docs() {
        return "Provides various methods for hooking into WorldEdit.";
    }

	// reserved LocalPlayer for the Console
	private static SKConsole console;

	public static SKCommandSender getSKPlayer(MCCommandSender sender, Target t) {
		SKCommandSender ret;
		if (sender == null || sender instanceof MCConsoleCommandSender) {
			if (console == null) {
				console = new SKConsole();
			}
			ret = console;
		} else if (sender instanceof MCPlayer) {
			ret = new SKPlayer((MCPlayer) sender);
		} else {
			throw new ConfigRuntimeException("Sender type not yet supported: " + sender.getClass().getName(),
					ExceptionType.CastException, t);
		}
		ret.setTarget(t);
		return ret;
	}

	public static Vector vtov(MVector3D vec) {
		return new Vector(vec.x, vec.y, vec.z);
	}

	public static MVector3D vtov(Vector vec) {
		return new MVector3D(vec.getX(), vec.getY(), vec.getZ());
	}

	public static WeightedBlockPattern generateBlockPattern(Construct source, Target t) {
		CArray src = Static.getArray(source, t);

		if (src.containsKey("name")) {
			int data = 0;
			double weight = 1D;
			if (src.containsKey("data")) {
				data = Static.getInt32(src.get("data", t), t);
			}
			if (src.containsKey("weight")) {
				weight = Static.getDouble(src.get("weight", t), t);
			}
			BlockType type = BlockType.lookup(src.get("name", t).val());
			if (type == null) {
				throw new ConfigRuntimeException("Could not determine blocktype from " + src.get("name", t).val(),
						ExceptionType.NotFoundException, t);
			} else {
				return new WeightedBlockPattern(new BaseBlock(type.getID(), data), weight);
			}
		} else {
			throw new ConfigRuntimeException("Block name required", ExceptionType.FormatException, t);
		}
	}

	public static WeightedBlockPattern generateBlockPattern(String source, Target t) {
		try {
			int data = 0;
			double weight = 1D;
			String[] typeInfo;
			String[] weightedType = source.split("%");
			if (weightedType.length == 1) {
				typeInfo = weightedType[0].split(":");
			} else {
				weight = Double.valueOf(weightedType[0]);
				typeInfo = weightedType[1].split(":");
			}
			BlockType type = BlockType.lookup(typeInfo[0]);
			if (typeInfo.length > 1) {
				data = Integer.valueOf(typeInfo[1]);
			}
			return new WeightedBlockPattern(new BaseBlock(type.getID(), data), weight);
		} catch (NumberFormatException e) {
			throw new ConfigRuntimeException(e.getMessage(), ExceptionType.CastException, t);
		}
	}

    @api(environments=CommandHelperEnvironment.class)
    public static class sk_pos1 extends SKFunction {

		@Override
        public String getName() {
            return "sk_pos1";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{0, 1, 2};
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

		@Override
        public String docs() {
            return "mixed {[player], array | [player]} Sets the player's point 1, or returns it if the array to set isn't specified." +
    				"Returns an array in format array(0:xValue, 1:yValue, 2:zValue, x:xValue, y:yValue, z:zValue) or null when the position has not been selected (coordinates 0,0,0).";
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender m = null;
            MVector3D v = null;
            Static.checkPlugin("WorldEdit", t);

            if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) { // If the command sender is a player.
                m = env.getEnv(CommandHelperEnvironment.class).GetPlayer(); // Get the command sender (MCPlayer).
            }
            if (args.length == 2) { // If sk_posX(player, locationArray).
				m = SKCompat.myGetPlayer(args[0], t);
                v = ObjectGenerator.GetGenerator().vector(args[1], t);
            } else if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    v = ObjectGenerator.GetGenerator().vector(args[0], t);
                } else {
                    m = Static.GetPlayer(args[0].val(), t);
                }
            }

            SKCommandSender user = getSKPlayer(m, t);

            RegionSelector sel = user.getLocalSession().getRegionSelector(user.getWorld());
            if (!( sel instanceof CuboidRegionSelector )) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(),
						ExceptionType.PluginInternalException, t);
            }
            if (v != null) {
            	
            	// Set the new point.
				sel.selectPrimary(new Vector(v.x, v.y, v.z), null);
				
				// Get instances to update WorldEdit CUI.
				WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Static.getServer().getPluginManager().getPlugin("WorldEdit").getHandle();
				
				Player player = (Player) m.getHandle(); // The bukkit player, required for player.sendPluginMessage().
				BukkitPlayer bukkitPlayer = new BukkitPlayer(worldEditPlugin, worldEditPlugin.getServerInterface(), player); // Has an implementation for dispatchCUIEvent(CUIEvent event).
				LocalSession localSession = user.getLocalSession();
				
				// Update WorldEdit CUI.
				localSession.setCUISupport(true);
				localSession.dispatchCUISetup(bukkitPlayer);
				
				// Return void as a new point has been selected.
				return CVoid.VOID;
				
            } else {
                Vector pt = ( (CuboidRegion) sel.getIncompleteRegion() ).getPos1();
                if (pt == null) {
                    throw new ConfigRuntimeException("Point in " + this.getName() + "undefined",
							ExceptionType.PluginInternalException, t);
                }
				CArray ret = ObjectGenerator.GetGenerator().vector(vtov(pt), t);
				
				// Return null when the position is not set (Coordinates 0,0,0).
				if(Float.parseFloat(ret.get("x", t).getValue()) == 0f && Float.parseFloat(ret.get("y", t).getValue()) == 0f && Float.parseFloat(ret.get("z", t).getValue()) == 0f) {
					return CNull.NULL;
				}
				
				// Set the x,y,z values to indices 0,1,2 (for backwards compatibility).
				ret.set("0", ret.get("x", t), t);
				ret.set("1", ret.get("y", t), t);
				ret.set("2", ret.get("z", t), t);
				
				// Return the point coordinates.
				return ret;
            }
        }
    }

    @api(environments=CommandHelperEnvironment.class)
    public static class sk_pos2 extends SKFunction {

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
			return "mixed {[player], array | [player]} Sets the player's point 2, or returns it if the array to set isn't specified." +
    				"Returns an array in format array(0:xValue, 1:yValue, 2:zValue, x:xValue, y:yValue, z:zValue) or null when the position has not been selected (coordinates 0,0,0).";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCCommandSender m = null;
            MVector3D v = null;
            Static.checkPlugin("WorldEdit", t);

            if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
            }
            if (args.length == 2) {
				m = SKCompat.myGetPlayer(args[0], t);
				v = ObjectGenerator.GetGenerator().vector(args[1], t);
            } else if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    v = ObjectGenerator.GetGenerator().vector(args[0], t);
                } else {
                    m = Static.GetPlayer(args[0].val(), t);
                }
            }

            SKCommandSender user = getSKPlayer(m, t);

            RegionSelector sel = user.getLocalSession().getRegionSelector(user.getWorld());
            if (!( sel instanceof CuboidRegionSelector )) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(),
						ExceptionType.PluginInternalException, t);
            }

            if (v != null) {
            	
            	// Set the new point.
                sel.selectSecondary(new Vector(v.x, v.y, v.z), null);
                
                // Get instances to update WorldEdit CUI.
				WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Static.getServer().getPluginManager().getPlugin("WorldEdit").getHandle();
				
				Player player = (Player) m.getHandle(); // The bukkit player, required for player.sendPluginMessage().
				BukkitPlayer bukkitPlayer = new BukkitPlayer(worldEditPlugin, worldEditPlugin.getServerInterface(), player); // Has an implementation for dispatchCUIEvent(CUIEvent event).
				LocalSession localSession = user.getLocalSession();
				
				// Update WorldEdit CUI.
				localSession.setCUISupport(true);
				localSession.dispatchCUISetup(bukkitPlayer);
				
				// Return void as a new point has been selected.
				return CVoid.VOID;
				
            } else {
                Vector pt = ( (CuboidRegion) sel.getIncompleteRegion() ).getPos2();
                if (pt == null) {
                    throw new ConfigRuntimeException("Point in " + this.getName() + "undefined",
							ExceptionType.PluginInternalException, t);
                }
                CArray ret = ObjectGenerator.GetGenerator().vector(vtov(pt), t);
				
				// Return null when the position is not set (Coordinates 0,0,0).
				if(Float.parseFloat(ret.get("x", t).getValue()) == 0f && Float.parseFloat(ret.get("y", t).getValue()) == 0f && Float.parseFloat(ret.get("z", t).getValue()) == 0f) {
					return CNull.NULL;
				}
				
				// Set the x,y,z values to indices 0,1,2 (for backwards compatibility).
				ret.set("0", ret.get("x", t), t);
				ret.set("1", ret.get("y", t), t);
				ret.set("2", ret.get("z", t), t);
				
				return ret;
            }
        }
    }

//    public static class sk_points extends SKFunction {
//
//        public String getName() {
//            return "sk_points";
//        }
//
//        public Integer[] numArgs() {
//            return new Integer[]{0, 1, 2};
//        }
//
//        public String docs() {
//            return "mixed {[player], arrayOfArrays | [player]} Sets a series of points, or returns the poly selection for this player, if one is specified."
//                    + " The array should be an array of arrays, and the arrays should be array(x, y, z)";
//        }
//
//        public ExceptionType[] thrown() {
//            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
//        }
//
//        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
//            Static.checkPlugin("WorldEdit", t);
//            return CVoid.VOID;
//        }
//    }

	@api(environments=CommandHelperEnvironment.class)
	public static class sk_setblock extends SKFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException,
					ExceptionType.PlayerOfflineException, ExceptionType.FormatException, ExceptionType.CastException };
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {

			Static.checkPlugin("WorldEdit", t);
			WorldEdit worldEdit = WorldEdit.getInstance();

			MCPlayer player = null;
			Construct pat = null;
			if (args.length == 2) {
				if (!(args[0] instanceof CNull)) {
					player = Static.GetPlayer(args[0], t);
				}
				pat = Static.getArray(args[1], t);
			} else {
				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}
				pat = Static.getArray(args[0], t);
			}
			SKCommandSender user = getSKPlayer(player, t);

			Pattern pattern;
			if (pat instanceof CArray) {
				CArray pata = (CArray) pat;
				if (pata.size() == 0) {
					pattern = new BlockPattern(new BaseBlock(0));
				} else if (pata.size() == 1) {
//					CArray singleBlock = Static.getArray(pata.get(0, t), t);
					pattern = generateBlockPattern(pata.get(0, t), t);
				} else {
					pattern = new RandomPattern();
					for (Construct entry : pata.asList()) {
						WeightedBlockPattern temp = generateBlockPattern(entry, t);
						((RandomPattern) pattern).add(temp, temp.getWeight());
					}
				}
			} else {
				String[] multi = pat.val().split(",");
				if (multi.length > 1) {
					pattern = new RandomPattern();
					for (String type : multi) {
						WeightedBlockPattern temp = generateBlockPattern(type, t);
						((RandomPattern) pattern).add(temp, temp.getWeight());
					}
				} else {
					pattern = generateBlockPattern(multi[0], t);
				}
			}

			try {
				new RegionCommands(worldEdit).set(user, user.getLocalSession(), user.getEditSession(false), pattern);
			} catch (WorldEditException wee) {
				throw new ConfigRuntimeException(wee.getMessage(), ExceptionType.PluginInternalException, t);
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
			return "void {[player], pattern} Sets a selection of blocks according to the provided pattern,"
					+ " a normal array of associative arrays. If the array is empty, the entire selection will be"
					+ " set to air. The inner arrays consist of a required 'name' field, an optional 'data' field,"
					+ " and an optional decimal 'weight' field. If data is not given it defaults to 0,"
					+ " and if weight is not given it defaults to 1. The weight represents that blocktype's chance"
					+ " of being selected for the next random block setting.";
		}
	}
//https://github.com/sk89q/WorldEdit/blob/7192780251dc71f5c70f2460d74eaee6a992333f/worldedit-core/src/main/java/com/sk89q/worldedit/command/SchematicCommands.java#L79-L131
	@api
	public static class skcb_load extends SKFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[] { ExceptionType.PluginInternalException, ExceptionType.PlayerOfflineException,
				ExceptionType.IOException, ExceptionType.InvalidPluginException
			};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {

			Static.checkPlugin("WorldEdit", t);
			WorldEdit worldEdit = WorldEdit.getInstance();
			String filename = args[0].val();
			MCPlayer player = null;
			if (args.length == 2) {
				player = Static.GetPlayer(args[1], t);
			}
			SKCommandSender user = getSKPlayer(player, t);

			File dir = worldEdit.getWorkingDirectoryFile(worldEdit.getConfiguration().saveDir);
			File f;

			try {
				f = worldEdit.getSafeOpenFile(user, dir, filename, "schematic", "schematic");
			} catch (FilenameException fne) {
				throw new ConfigRuntimeException(fne.getMessage(), ExceptionType.IOException, t);
			}

			if (!f.exists()) {
				throw new ConfigRuntimeException("Schematic " + filename + " does not exist!",
						ExceptionType.IOException, t);
			}

			Closer closer = Closer.create();
			try {
				String filePath = f.getCanonicalPath();
				String dirPath = dir.getCanonicalPath();

				if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
					throw new ConfigRuntimeException("Clipboard file could not read or it does not exist.",
							ExceptionType.IOException, t);
				} else {
					FileInputStream fis = closer.register(new FileInputStream(f));
					BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
					ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(bis);

					WorldData worldData = user.getWorld().getWorldData();
					Clipboard clipboard = reader.read(worldData);
					user.getLocalSession().setClipboard(new ClipboardHolder(clipboard, worldData));
				}
			} catch (IOException e) {
				throw new ConfigRuntimeException("Schematic could not read or it does not exist: " + e.getMessage(),
						ExceptionType.IOException, t);
			} finally {
				try {
					closer.close();
				} catch (IOException ignored) {
				}
			}
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
	public static class skcb_rotate extends SKFunction {

		@Override
		public Exceptions.ExceptionType[] thrown() {
			return new Exceptions.ExceptionType[]{ExceptionType.InvalidPluginException,
					Exceptions.ExceptionType.NotFoundException, ExceptionType.PlayerOfflineException,
					ExceptionType.RangeException, Exceptions.ExceptionType.CastException};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.checkPlugin("WorldEdit", t);
			int yaxis = 0,
					xaxis = 0,
					zaxis = 0;
			MCPlayer plyr = null;
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
					plyr = Static.GetPlayer(args[0], t);
					break;
			}
			try {
				ClipboardCommands command = new ClipboardCommands(WorldEdit.getInstance());
				SKCommandSender user = getSKPlayer(plyr, t);
				command.rotate(user, user.getLocalSession(), (double) yaxis, (double) xaxis, (double) zaxis);
			} catch (EmptyClipboardException e) {
				throw new ConfigRuntimeException("The clipboard is empty, copy something to it first!",
						Exceptions.ExceptionType.NotFoundException, t);
			} catch (WorldEditException ex) {
				throw new ConfigRuntimeException(ex.getMessage(),
						Exceptions.ExceptionType.PluginInternalException, t);
			}
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
	public static class skcb_paste extends SKFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ ExceptionType.InvalidWorldException, ExceptionType.FormatException,
				ExceptionType.NotFoundException, ExceptionType.RangeException,
				ExceptionType.CastException, ExceptionType.InvalidPluginException
			};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.checkPlugin("WorldEdit", t);
			boolean airless = false,
					fastMode = false,
					origin = false,
					select = false;
			SKCommandSender user = null;
			if (args[0] instanceof CArray) {
				user = getSKPlayer(null, t);
				user.setLocation(ObjectGenerator.GetGenerator().location(args[0], null, t));
			} else {
				user = getSKPlayer(Static.GetPlayer(args[0], t), t);
			}
			if (args.length >= 2) {
				CArray options = Static.getArray(args[1], t);
				if (options.containsKey("airless")) {
					airless = Static.getBoolean(options.get("airless", t));
				}
				if (options.containsKey("fastmode")) {
					fastMode = Static.getBoolean(options.get("fastmode", t));
				}
				if (options.containsKey("origin")) {
					origin = Static.getBoolean(options.get("origin", t));
				}
				if (options.containsKey("select")) {
					select = Static.getBoolean(options.get("select", t));
				}
			}
			EditSession editor = user.getEditSession(fastMode);

			try {
				ClipboardCommands command = new ClipboardCommands(WorldEdit.getInstance());
				command.paste(user, user.getLocalSession(), editor, airless, origin, select);
			} catch (MaxChangedBlocksException e) {
				throw new ConfigRuntimeException("Attempted to change more blocks than allowed.",
						Exceptions.ExceptionType.RangeException, t);
			} catch (EmptyClipboardException e) {
				throw new ConfigRuntimeException("The clipboard is empty, copy something to it first!",
						Exceptions.ExceptionType.NotFoundException, t);
			} catch (WorldEditException ex) {
				Logger.getLogger(SKWorldEdit.class.getName()).log(Level.SEVERE, null, ex);
			}

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
			return "void {location, [array] | player, [array]}"
					+ " Pastes a schematic from the player's clipboard if a player is provided,"
					+ " or from the console's clipboard if a location is given, as if a player was standing there."
					+ " An associative array of options can be provided, all of which default to false."
					+ " If 'airless' is true, air blocks from the schematic will not replace blocks in the world."
					+ " If 'fastmode' is true, the function will use WorldEdit's 'fastmode' to paste."
					+ " If 'origin' is true, the schematic will be pasted at the original location it was copied from."
					+ " If 'select' is true, the pasted blocks will be automatically selected."
					+ " Both ignoreAir and entities default to false.";
		}
	}
}
