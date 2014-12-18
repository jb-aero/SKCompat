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
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
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
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.command.ClipboardCommands;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.zeoldcraft.skcompat.SKCompat.SKFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jb_aero
 */
public class SKWorldEdit {

    public static String docs() {
        return "Provides various methods for programmatically hooking into WorldEdit.";
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
	private static ConsoleSKPlayer player;
	// CH's console-based session
	private static LocalSession session;
	// CH's console-based editsession, for logging purposes
	private static EditSession edits;

	public static ConsoleSKPlayer getSKPlayer(Target t) {
		if (player == null) {
			player = new ConsoleSKPlayer();
		}
		player.setTarget(t);
		return player;
	}

	public static LocalSession getLocalSession(Target t) {
		if (session == null) {
			session = WorldEdit.getInstance().getSessionManager().get(getSKPlayer(t));
		}
		return session;
	}
	
	public static EditSession getEditSession(boolean fastMode, Target t) {
		if (edits == null) {
			edits = WorldEdit.getInstance().getEditSessionFactory()
					.getEditSession(getSKPlayer(t).getWorld(), -1, null, getSKPlayer(t));
		}
		edits.setFastMode(fastMode);
		return edits;
	}

	@api
	public static class skcb_load extends SKFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[] { ExceptionType.PluginInternalException,
				ExceptionType.IOException, ExceptionType.InvalidPluginException
			};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.checkPlugin("WorldEdit", t);
			// Based on: com.sk89q.worldedit.commands.SchematicCommands.load
			String filename = args[0].val();
			try {
				SchematicCommands command = new SchematicCommands(WorldEdit.getInstance());
				command.load(getSKPlayer(t), getLocalSession(t), "schematic", filename);
			} catch (FilenameException e) {
				throw new ConfigRuntimeException(e.getMessage(), Exceptions.ExceptionType.PluginInternalException, t);
			}
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "skcb_load";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {filename} Loads a schematic into the clipboard from file."
					+ " It will use the directory specified in WorldEdit's config.";
		}
	}

	@api
	public static class skcb_rotate extends SKFunction {

		@Override
		public Exceptions.ExceptionType[] thrown() {
			return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.RangeException, Exceptions.ExceptionType.NotFoundException,
					Exceptions.ExceptionType.InvalidPluginException, Exceptions.ExceptionType.CastException};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.checkPlugin("WorldEdit", t);
			double xaxis = Static.getInt32(args[0], t),
					yaxis = Static.getInt32(args[1], t),
					zaxis = Static.getInt32(args[2], t);
			try {
				ClipboardCommands command = new ClipboardCommands(WorldEdit.getInstance());
				command.rotate(getSKPlayer(t), getLocalSession(t), yaxis, xaxis, zaxis);
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
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "void {int x-axis, int y-axis, int z-axis}"
					+ " Rotates the clipboard by the given (multiple of 90)"
					+ " degrees for each corresponding axis. To skip an axis,"
					+ " simply give it a value of 0.";
		}
	}

	@api
	public static class skcb_paste extends SKFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ ExceptionType.InvalidWorldException,
				ExceptionType.NotFoundException, ExceptionType.RangeException,
				ExceptionType.CastException, ExceptionType.InvalidPluginException
			};
		}

		@Override
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Static.checkPlugin("WorldEdit", t);
			boolean airless = false,
					fastmode = false,
					origin = false,
					select = false;
			if (args.length >= 2) {
				if (args[1] instanceof CArray) {
					CArray options = (CArray) args[1];
					if (options.containsKey("airless")) {
						airless = Static.getBoolean(options.get("airless", t));
					}
					if (options.containsKey("fastmode")) {
						fastmode = Static.getBoolean(options.get("fastmode", t));
					}
					if (options.containsKey("origin")) {
						origin = Static.getBoolean(options.get("origin", t));
					}
					if (options.containsKey("select")) {
						select = Static.getBoolean(options.get("select", t));
					}
				} else {
					throw new Exceptions.FormatException("Arg 2 of "
							+ getName() + " expected an array.", t);
				}
			}
			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], null, t);
			getSKPlayer(t).setLocation(loc);
			EditSession editor = getEditSession(fastmode, t);

			try {
				ClipboardCommands command = new ClipboardCommands(WorldEdit.getInstance());
				command.paste(getSKPlayer(t), getLocalSession(t), editor, airless, origin, select);
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
			return "void {location, [array]}"
					+ " Pastes a schematic from the clipboard as if a player was standing at the location,"
					+ " An associative array of options can be provided, all of which default to false."
					+ " If 'airless' is true, air blocks from the schematic will not replace blocks in the world."
					+ " If 'fastmode' is true, the function will use WorldEdit's 'fastmode' to paste."
					+ " If 'origin' is true, *TEST*."
					+ " If 'select' is true, the pasted blocks will be automatically selected."
					+ " Both ignoreAir and entities default to false.";
		}
	}
}
