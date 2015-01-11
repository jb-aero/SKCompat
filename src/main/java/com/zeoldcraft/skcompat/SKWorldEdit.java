package com.zeoldcraft.skcompat;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.SKHandler;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.command.ClipboardCommands;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
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

/**
 *
 * @author jb_aero
 */
public class SKWorldEdit {

    public static String docs() {
        return "Provides various methods for hooking into WorldEdit.";
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
            return "mixed {[player], locationArray | [player]} Sets the player's point 1, or returns it if the array to set isn't specified. If"
                    + " the location is returned, it is returned as a 4 index array:(x, y, z, world)";
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCPlayer m = null;
            MCLocation l = null;
            Static.checkPlugin("WorldEdit", t);

            if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                l = ObjectGenerator.GetGenerator().location(args[1], m.getWorld(), t);
            } else if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    l = ObjectGenerator.GetGenerator().location(args[0], ( m == null ? null : m.getWorld() ), t);
                } else {
                    m = Static.GetPlayer(args[0].val(), t);
                }
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }

            RegionSelector sel = SKHandler.getWorldEditPlugin(t).getSession(( (BukkitMCPlayer) m )._Player()).getRegionSelector(BukkitUtil.getLocalWorld(( (BukkitMCWorld) m.getWorld() ).__World()));
            if (!( sel instanceof CuboidRegionSelector )) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, t);
            }
            if (l != null) {
				sel.selectPrimary(BukkitUtil.toVector(( (BukkitMCLocation) l )._Location()), null);
                return CVoid.VOID;
            } else {
                Vector pt = ( (CuboidRegion) sel.getIncompleteRegion() ).getPos1();
                if (pt == null) {
                    throw new ConfigRuntimeException("Point in " + this.getName() + "undefined", ExceptionType.PluginInternalException, t);
                }
                return new CArray(t,
                        new CInt(pt.getBlockX(), t),
                        new CInt(pt.getBlockY(), t),
                        new CInt(pt.getBlockZ(), t),
                        new CString(m.getWorld().getName(), t));
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
            return "mixed {[player], array | [player]} Sets the player's point 2, or returns it if the array to set isn't specified";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.CastException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            MCPlayer m = null;
            MCLocation l = null;
            Static.checkPlugin("WorldEdit", t);

            if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
            }
            if (args.length == 2) {
                m = Static.GetPlayer(args[0].val(), t);
                l = ObjectGenerator.GetGenerator().location(args[1], m.getWorld(), t);
            } else if (args.length == 1) {
                if (args[0] instanceof CArray) {
                    l = ObjectGenerator.GetGenerator().location(args[0], ( m == null ? null : m.getWorld() ), t);
                } else {
                    m = Static.GetPlayer(args[0].val(), t);
                }
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }

            RegionSelector sel = SKHandler.getWorldEditPlugin(t).getSession(( (BukkitMCPlayer) m )._Player()).getRegionSelector(BukkitUtil.getLocalWorld(( (BukkitMCWorld) m.getWorld() ).__World()));
            if (!( sel instanceof CuboidRegionSelector )) {
                throw new ConfigRuntimeException("Only cuboid regions are supported with " + this.getName(), ExceptionType.PluginInternalException, t);
            }

            if (l != null) {
                sel.selectSecondary(BukkitUtil.toVector(( (BukkitMCLocation) l )._Location()), null);
                return CVoid.VOID;
            } else {
                Vector pt = ( (CuboidRegion) sel.getIncompleteRegion() ).getPos2();
                if (pt == null) {
                    throw new ConfigRuntimeException("Point in " + this.getName() + "undefined", ExceptionType.PluginInternalException, t);
                }
                return new CArray(t,
                        new CInt(pt.getBlockX(), t),
                        new CInt(pt.getBlockY(), t),
                        new CInt(pt.getBlockZ(), t),
                        new CString(m.getWorld().getName(), t));
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
	
	/******************Clipboard stuff below this line******************/

	// CH's local player, based from console
	private static SKConsole console;

	public static SKCommandSender getSKPlayer(MCPlayer player, Target t) {
		SKCommandSender sender;
		if (player == null) {
			if (console == null) {
				console = new SKConsole();
			}
			sender = console;
		} else {
			sender = new SKPlayer(player);
		}
		sender.setTarget(t);
		return sender;
	}

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
			/* Adapted from:
			 * https://github.com/sk89q/WorldEdit/blob/master/worldedit-core/src/main/java/
			 * com/sk89q/worldedit/command/SchematicCommands.java#L79-L131
			 */
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
					+ " If 'origin' is true, *TEST*."
					+ " If 'select' is true, the pasted blocks will be automatically selected."
					+ " Both ignoreAir and entities default to false.";
		}
	}
}
