package com.zeoldcraft.skcompat;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.MVector3D;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.SKHandler;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.InvalidEnvironmentException;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zeoldcraft.skcompat.SKCompat.SKFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

/**
 *
 */
public class SKWorldGuard {

    public static String docs() {
        return "Provides various methods for hooking into WorldGuard";
    }
	
	@api
	public static class sk_region_info extends SKFunction {

		@Override
		public String getName() {
			return "sk_region_info";
		}

		@Override
        public Version since() {
            return CHVersion.V3_2_0;
        }

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "array {region, world, [value]} Given a region name, returns an array of information about that region."
					+ " ---- If value is set, it should be an integer of one of the following indexes, and only that information for that index"
					+ " will be returned. Otherwise if value is not specified (or is -1), it returns an array of"
					+ " information with the following pieces of information in the specified index:<ul>"
					+ " <li>0 - An array of points that define this region</li>"
					+ " <li>1 - An array of owners of this region</li>"
					+ " <li>2 - An array of members of this region</li>"
					+ " <li>3 - An array of arrays of this region's flags, where each array is: array(flag_name, value)</li>"
					+ " <li>4 - This region's priority</li>"
					+ " <li>5 - The volume of this region (in meters cubed)</li>"
					+ "</ul>"
					+ "If the region cannot be found, a PluginInternalException is thrown.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PluginInternalException,
				ExceptionType.CastException, ExceptionType.RangeException};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			try {
				String regionName = args[0].val();
				String worldName = args[1].val();
				int index = -1;

				if (args.length == 3) {
					index = Static.getInt32(args[2], t);
				}

				int maxIndex = 5;
				if (index < -1 || index > maxIndex) {
					throw new ConfigRuntimeException(this.getName() + " expects the index to be between -1 and " + maxIndex,
							ExceptionType.RangeException, t);
				}

				org.bukkit.World world = Bukkit.getServer().getWorld(worldName);
				if (world == null) {
					throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
				}
				RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);
				ProtectedRegion region = mgr.getRegion(regionName);
				if (region == null) {
					throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, t);
				}

				CArray ret = new CArray(t);

				//Fill these data structures in with the information we need
				if (index == 0 || index == -1) {
					List<Location> points = new ArrayList<>();

					boolean first = true;
					if (region instanceof ProtectedPolygonalRegion) {
						for (BlockVector2D pt : ((ProtectedPolygonalRegion) region).getPoints()) {
							points.add(new Location(world, pt.getX(), first ? region.getMaximumPoint().getY()
									: region.getMinimumPoint().getY(), pt.getZ()));
							first = false;
						}
					} else {
						points.add(com.sk89q.worldguard.bukkit.BukkitUtil.toLocation(world, region.getMaximumPoint()));
						points.add(com.sk89q.worldguard.bukkit.BukkitUtil.toLocation(world, region.getMinimumPoint()));
					}

					CArray pointSet = new CArray(t);
					for (Location l : points) {
						CArray point = new CArray(t);
						point.push(new CInt(l.getBlockX(), t));
						point.push(new CInt(l.getBlockY(), t));
						point.push(new CInt(l.getBlockZ(), t));
						point.push(new CString(l.getWorld().getName(), t));
						pointSet.push(point);
					}

					ret.push(pointSet);
				}

				if (index == 1 || index == -1) {
					List<UUID> ownersPlayers = new ArrayList<>();
					List<String> ownersGroups = new ArrayList<>();
					ownersPlayers.addAll(region.getOwners().getUniqueIds());
					ownersGroups.addAll(region.getOwners().getGroups());

					CArray ownerSet = CArray.GetAssociativeArray(t);
					CArray players = new CArray(t);
					CArray groups = new CArray(t);
					for (UUID member : ownersPlayers) {
						players.push(new CString(member.toString(), t));
					}
					for (String member : ownersGroups) {
						groups.push(new CString(member, t));
					}
					ownerSet.set("players", players, t);
					ownerSet.set("groups", groups, t);

					ret.push(ownerSet);
				}

				if (index == 2 || index == -1) {
					List<UUID> membersPlayers = new ArrayList<>();
					List<String> membersGroups = new ArrayList<>();
					membersPlayers.addAll(region.getMembers().getUniqueIds());
					membersGroups.addAll(region.getMembers().getGroups());

					CArray memberSet = CArray.GetAssociativeArray(t);
					CArray players = new CArray(t);
					CArray groups = new CArray(t);
					for (UUID member : membersPlayers) {
						players.push(new CString(member.toString(), t));
					}
					for (String member : membersGroups) {
						groups.push(new CString(member, t));
					}
					memberSet.set("players", players, t);
					memberSet.set("groups", groups, t);
					ret.push(memberSet);
				}

				if (index == 3 || index == -1) {
					Map<String, String> flags = new HashMap<>();
					for (Map.Entry<Flag<?>, Object> ent : region.getFlags().entrySet()) {
						flags.put(ent.getKey().getName(), String.valueOf(ent.getValue()));
					}

					CArray flagSet = new CArray(t);
					for (Map.Entry<String, String> flag : flags.entrySet()) {
						CArray fl = new CArray(t,
								new CString(flag.getKey(), t),
								new CString(flag.getValue(), t));
						flagSet.push(fl);
					}

					ret.push(flagSet);
				}

				if (index == 4 || index == -1) {
					int priority;
					priority = region.getPriority();
					ret.push(new CInt(priority, t));
				}

				if (index == 5 || index == -1) {
					float volume;
					volume = region.volume();
					ret.push(new CDouble(volume, t));
				}

				if (ret.size() == 1) {
					return ret.get(0, t);
				}
				return ret;

			} catch (NoClassDefFoundError e) {
				throw new ConfigRuntimeException("It does not appear as though the WorldEdit or WorldGuard plugin is loaded properly. Execution of " + this.getName() + " cannot continue.", ExceptionType.InvalidPluginException, t, e);
			}
		}
	}

    @api
    public static class sk_region_overlaps extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_overlaps";
        }

		@Override
        public Version since() {
            return CHVersion.V3_2_0;
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{3};
        }

		@Override
        public String docs() {
            return "boolean {world, region1, array(region2, [regionN...])} Returns true or false whether or not the specified regions overlap.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String region1 = args[1].val();
            List<ProtectedRegion> checkRegions = new ArrayList<>();
            Static.checkPlugin("WorldGuard", t);
            World world = Bukkit.getServer().getWorld(args[0].val());
            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
            }
            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);
            if (args[2] instanceof CArray) {
                CArray arg = (CArray) args[2];
                for (int i = 0; i < arg.size(); i++) {
                    ProtectedRegion region = mgr.getRegion(arg.get(i, t).val());
                    if (region == null) {
                        throw new ConfigRuntimeException("Region " + arg.get(i, t).val() + " could not be found!", ExceptionType.PluginInternalException, t);
                    }
                    checkRegions.add(region);
                }
            } else {
                ProtectedRegion region = mgr.getRegion(args[2].val());
                if (region == null) {
                    throw new ConfigRuntimeException("Region " + args[2] + " could not be found!", ExceptionType.PluginInternalException, t);
                }
                checkRegions.add(region);
            }

            ProtectedRegion region = mgr.getRegion(region1);
            if (region == null) {
                throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, t);
            }

            try {
                if (!region.getIntersectingRegions(checkRegions).isEmpty()) {
                    return CBoolean.TRUE;
                }
            } catch (Exception e) {
            }
            return CBoolean.FALSE;
        }
    }

    @api
    public static class sk_region_intersect extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_intersect";
        }

		@Override
        public Version since() {
            return CHVersion.V3_2_0;
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

		@Override
        public String docs() {
            return "array {world, region1, [array(region2, [regionN...])]} Returns an array of regions names which intersect with defined region."
					+ " You can pass an array of regions to verify or omit this parameter and all regions in selected world will be checked.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String region1 = args[1].val();
            List<ProtectedRegion> checkRegions = new ArrayList<>();
			List<ProtectedRegion> getRegions;
			CArray listRegions = new CArray(t);

            Static.checkPlugin("WorldGuard", t);
            World world = Bukkit.getServer().getWorld(args[0].val());
            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
            }

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

			if (args.length == 2) {
				 checkRegions.addAll(mgr.getRegions().values());
			} else {
				if (args[2] instanceof CArray) {
					CArray arg = (CArray) args[2];
					for (int i = 0; i < arg.size(); i++) {
						ProtectedRegion region = mgr.getRegion(arg.get(i, t).val());
						if (region == null) {
							throw new ConfigRuntimeException(String.format("Region %s could not be found!", arg.get(i, t).val()), ExceptionType.PluginInternalException, t);
						}
						checkRegions.add(region);
					}
				} else {
					ProtectedRegion region = mgr.getRegion(args[2].val());
					if (region == null) {
						throw new ConfigRuntimeException(String.format("Region %s could not be found!", args[2]), ExceptionType.PluginInternalException, t);
					}
					checkRegions.add(region);
				}
			}

            ProtectedRegion region = mgr.getRegion(region1);
            if (region == null) {
                throw new ConfigRuntimeException(String.format("Region %s could not be found!", region1), ExceptionType.PluginInternalException, t);
            }

            try {
				getRegions = region.getIntersectingRegions(checkRegions);

                if (!getRegions.isEmpty()) {
					for (ProtectedRegion r : getRegions) {
						if (args.length != 2 || !r.getId().equals(region.getId())) {
							listRegions.push(new CString(r.getId(), t));
						}
					}

					if (listRegions.isEmpty()) {
						return new CArray(t);
					}

					return listRegions;
                }
            } catch (Exception e) {
            }
            return new CArray(t);
        }
    }

    @api
    public static class sk_all_regions extends SKFunction {

		@Override
        public String getName() {
            return "sk_all_regions";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

		@Override
        public String docs() {
            return "array {[world]} Returns all the regions in all worlds, or just the one world, if specified.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            List<World> checkWorlds = null;
            CArray arr = new CArray(t);
            if (args.length == 1) {
                World world = Bukkit.getServer().getWorld(args[0].val());
                if (world != null) {
                    checkWorlds = Arrays.asList(world);
                }
            }
            if (checkWorlds == null) {
                checkWorlds = Bukkit.getServer().getWorlds();
            }
            for (World world : checkWorlds) {
                for (String region : SKHandler.getWorldGuardPlugin(t).getRegionManager(world).getRegions().keySet()) {
                    arr.push(new CString(region, t));
                }
            }
            return arr;

        }
    }

    @api(environments=CommandHelperEnvironment.class)
    public static class sk_current_regions extends SKFunction {

		@Override
        public String getName() {
            return "sk_current_regions";
        }

		@Override
        public Version since() {
            return CHVersion.V3_2_0;
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{0, 1};
        }

		@Override
        public String docs() {
            return "mixed {[player]} Returns the list regions that player is in. If no player specified, then the current player is used."
                    + " If region is found, an array of region names are returned, else an empty array is returned";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            World world;

            MCPlayer m = null;

            if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
            }
            if (args.length == 1) {
                m = Static.GetPlayer(args[0].val(), t);
            }

            if (m == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a player", ExceptionType.PlayerOfflineException, t);
            }

            world = Bukkit.getServer().getWorld(m.getWorld().getName());

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);
            Vector pt = new Vector(m.getLocation().getBlockX(), m.getLocation().getBlockY(), m.getLocation().getBlockZ());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            CArray regions = new CArray(t);

            List<ProtectedRegion> sortedRegions = new ArrayList<>();

            for (ProtectedRegion r : set) {
                boolean placed = false;
                for (int i = 0; i < sortedRegions.size(); i++) {
                    if (sortedRegions.get(i).volume() < r.volume()) {
                        sortedRegions.add(i, r);
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    sortedRegions.add(r);
                }
            }

            for (ProtectedRegion region : sortedRegions) {
                regions.push(new CString(region.getId(), t));
            }

            if (regions.size() > 0) {
                return regions;
            }

            return new CArray(t);
        }
    }

    @api(environments=CommandHelperEnvironment.class)
    public static class sk_regions_at extends SKFunction {

		@Override
        public String getName() {
            return "sk_regions_at";
        }

		@Override
        public Version since() {
            return CHVersion.V3_2_0;
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1};
        }

		@Override
        public String docs() {
            return "mixed {Locationarray} Returns a list of regions at the specified location. "
                    + "If regions are found, an array of region names are returned, otherwise, an empty array is returned.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.CastException, ExceptionType.PluginInternalException, ExceptionType.InsufficientArgumentsException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            World world;

            if (!( args[0] instanceof CArray )) {
                throw new ConfigRuntimeException(this.getName() + " needs a locationarray", ExceptionType.CastException, t);
            }

            MCWorld w = null;
            MCCommandSender c = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
            if (c instanceof MCPlayer) {
                w = ((MCPlayer)c).getWorld();
            }

            MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);

            if (loc.getWorld() == null) {
                throw new ConfigRuntimeException(this.getName() + " needs a world", ExceptionType.InsufficientArgumentsException, t);
            }

            world = Bukkit.getServer().getWorld(loc.getWorld().getName());

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);
            Vector pt = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            CArray regions = new CArray(t);

            List<ProtectedRegion> sortedRegions = new ArrayList<>();

            for (ProtectedRegion r : set) {
                boolean placed = false;
                for (int i = 0; i < sortedRegions.size(); i++) {
                    if (sortedRegions.get(i).volume() < r.volume()) {
                        sortedRegions.add(i, r);
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    sortedRegions.add(r);
                }
            }

            for (ProtectedRegion region : sortedRegions) {
                regions.push(new CString(region.getId(), t));
            }

            if (regions.size() > 0) {
                return regions;
            }

            return new CArray(t);
        }
    }

    @api
    public static class sk_region_volume extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_volume";
        }

		@Override
        public Version since() {
            return CHVersion.V3_2_0;
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{2};
        }

		@Override
        public String docs() {
            return "int {region, world} Returns the volume of the given region in the given world.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);
            World world;

            world = Bukkit.getServer().getWorld(args[1].val());

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion region = mgr.getRegion(args[0].val());

            if (region == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", args[0].val(), args[1].val()), ExceptionType.PluginInternalException, t);
            }

            return new CInt(region.volume(), t);
        }
    }

	@api
	public static class sk_region_create extends SKFunction {

		@Override
		public String getName() {
			return "sk_region_create";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		@Override
		public String docs() {
			return "void {[world], name, array(locationArrayPos1, locationArrayPos2, [[locationArrayPosN]...])|[world], '__global__'}"
					+ " Create region of the given name in the given world. You can omit list of points if you want to create a __global__ region.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			Static.checkPlugin("WorldGuard", t);

			MCWorld w = null;
			String region;
			CArray points;
			List<BlockVector> verticies = new ArrayList<>();

			if ((args.length == 3) || (args.length == 2 && "__global__".equalsIgnoreCase(args[1].val()))) {

				w = Static.getServer().getWorld(args[0].val());
				region = args[1].val();

				if (w == null) {
					throw new ConfigRuntimeException("The specified world \"" + args[0].val() + "\" is not a valid world.", ExceptionType.InvalidWorldException, t);
				}
			} else {
				region = args[0].val();
			}

			if (!"__global__".equalsIgnoreCase(region)) {

				if ((args.length == 1) || !(args[args.length - 1] instanceof CArray)) {
					throw new ConfigRuntimeException("Pass an array of LocationArrays for a new region.", ExceptionType.PluginInternalException, t);
				}

				points = (CArray) args[args.length - 1];

				if (points.isEmpty()) {
					throw new ConfigRuntimeException("Expecting an array of LocationArrays but found none.", ExceptionType.PluginInternalException, t);
				}

				for (int i = 0; i < points.size(); i++) {

					if (!(points.get(i, t) instanceof CArray)) {
						throw new ConfigRuntimeException("LocationArrays must be arrays.", ExceptionType.PluginInternalException, t);
					}

					CArray point = (CArray) points.get(i, t);

					if (point.size() >= 3) {

						double x = 0;
						double y = 0;
						double z = 0;

						if (!point.inAssociativeMode()) {
							x = Static.getNumber(point.get(0, t), t);
							y = Static.getNumber(point.get(1, t), t);
							z = Static.getNumber(point.get(2, t), t);
						}

						if (point.containsKey("x")) {
							x = Static.getNumber(point.get("x", t), t);
						}
						if (point.containsKey("y")) {
							y = Static.getNumber(point.get("y", t), t);
						}
						if (point.containsKey("z")) {
							z = Static.getNumber(point.get("z", t), t);
						}

						verticies.add(new BlockVector(x, y, z));
					}
				}

				if (verticies.isEmpty()) {
					throw new ConfigRuntimeException("Expecting an array of LocationArrays but found no valid Location arrays.", ExceptionType.PluginInternalException, t);
				}
				
				if (w == null) {
					for (int i = 0; i < points.size(); i++) {

						CArray point = (CArray) points.get(i, t);

						if (point.size() >= 4) {

							MCWorld world = null;

							if (!point.inAssociativeMode()) {
								world = Static.getServer().getWorld(point.get(3, t).val());
							}

							if (point.containsKey("world")) {
								world = Static.getServer().getWorld(point.get("world", t).val());
							}
							
							if (world != null) {
								if (w == null) {
									w = world;
								} else {
									if (!w.equals(world)) {
										throw new ConfigRuntimeException(String.format("Conflicting worlds in LocationArrays."
												+ " (%s) & (%s)",w.getName(),world.getName()), ExceptionType.InvalidWorldException, t);
									}
								}
							}
						}
					}
				}
			}

			if (w == null){
				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					w = env.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
				}
			}

			if (w == null) {
				throw new ConfigRuntimeException("No world specified.", ExceptionType.InvalidWorldException, t);
			}

			RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(((BukkitMCWorld) w).__World());

			ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists != null) {
				throw new ConfigRuntimeException(String.format("The region (%s) already exists in world (%s),"
						+ " and cannot be created again.", region, w.getName()), ExceptionType.PluginInternalException, t);
			}

			ProtectedRegion newRegion;

			if ("__global__".equalsIgnoreCase(region)) {
				newRegion = new GlobalProtectedRegion(region);
			} else {
				if (verticies.size() == 2) {
					newRegion = new ProtectedCuboidRegion(region, verticies.get(0), verticies.get(1));
				} else {

					List<BlockVector2D> pointsPoly = new ArrayList<>();
					int minY = 0;
					int maxY = 0;

					for (int i = 0; i < verticies.size(); i++) {

						BlockVector vector = verticies.get(i);

						int x = vector.getBlockX();
						int y = vector.getBlockY();
						int z = vector.getBlockZ();

						pointsPoly.add(new BlockVector2D(x, z));

						if (i == 0) {
							minY = maxY = y;
						} else {
							if (y < minY) {
								minY = y;
							} else if (y > maxY) {
								maxY = y;
							}
						}
					}
					newRegion = new ProtectedPolygonalRegion(region, pointsPoly, minY, maxY);
				}

				if (newRegion == null) {
					throw new ConfigRuntimeException("Error while creating protected region", ExceptionType.PluginInternalException, t);
				}
			}

			mgr.addRegion(newRegion);

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while creating protected region", ExceptionType.PluginInternalException, t, e);
			}

			return CVoid.VOID;
		}
	}

    @api
    public static class sk_region_update extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_update";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

		@Override
        public String docs() {
            return "void {[world], name, array(vectorArrayPos1, vectorArrayPos2, [[vectorArrayPosN]...])} Updates the location of a given region to the new location. Other properties of the region, like owners, members, priority, etc are unaffected.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            String region;

            if (args.length == 2) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }
            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

			if ("__global__".equalsIgnoreCase(region)) {
				throw new ConfigRuntimeException("You cannot change position of __global__ region.", ExceptionType.PluginInternalException, t);
			}

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion oldRegion = mgr.getRegion(region);

            if (oldRegion == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

            if (!(args[args.length - 1] instanceof CArray)) {
                throw new ConfigRuntimeException("Pass an array of points to define a new region", ExceptionType.PluginInternalException, t);
            }

            List<BlockVector> points = new ArrayList<>();
            List<BlockVector2D> points2D = new ArrayList<>();

            int minY = 0;
            int maxY = 0;

            ProtectedRegion newRegion;

            CArray arg = (CArray) args[args.length - 1];

            for (int i = 0; i < arg.size(); i++) {
                MVector3D vec = ObjectGenerator.GetGenerator().vector(arg.get(i, t), t);

               if (arg.size() == 2) {
                    points.add(new BlockVector(vec.x, vec.y, vec.z));
                } else {
                    points2D.add(new BlockVector2D(vec.x, vec.z));

                    if (i == 0) {
                        minY = maxY = (int) vec.y;
                    } else {
                        if (vec.y < minY) {
                            minY = (int) vec.y;
                        } else if (vec.y > maxY) {
                            maxY = (int) vec.y;
                        }
                    }
                }
            }

            if (arg.size() == 2) {
                newRegion = new ProtectedCuboidRegion(region, points.get(0), points.get(1));
            } else {
                newRegion = new ProtectedPolygonalRegion(region, points2D, minY, maxY);
            }

            if (newRegion == null) {
                throw new ConfigRuntimeException("Error while redefining protected region", ExceptionType.PluginInternalException, t);
            }

			mgr.addRegion(newRegion);

			newRegion.setMembers(oldRegion.getMembers());
			newRegion.setOwners(oldRegion.getOwners());
			newRegion.setFlags(oldRegion.getFlags());
			newRegion.setPriority(oldRegion.getPriority());
			try {
				newRegion.setParent(oldRegion.getParent());
			} catch (ProtectedRegion.CircularInheritanceException ex) {
				// This won't happen because the settings will be the same
			}

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while redefining protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

    @api
    public static class sk_region_rename extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_rename";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

		@Override
        public String docs() {
            return "void {[world], oldName, newName} Rename the existing region. Other properties of the region, like owners, members, priority, etc are unaffected.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            String oldRegionName;
			String newRegionName;

            if (args.length == 2) {

                oldRegionName = args[0].val();
				newRegionName = args[1].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }
            } else {
                oldRegionName = args[1].val();
				newRegionName = args[2].val();
                world = Bukkit.getServer().getWorld(args[0].val());
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

			if ("__global__".equalsIgnoreCase(oldRegionName)) {
				throw new ConfigRuntimeException("You cannot change name of __global__ region.", ExceptionType.PluginInternalException, t);
			}

			if ("__global__".equalsIgnoreCase(newRegionName)) {
				throw new ConfigRuntimeException("You cannot change name of any region to __global__.", ExceptionType.PluginInternalException, t);
			}

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion oldRegion = mgr.getRegion(oldRegionName);

            if (oldRegion == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", oldRegionName, world.getName()), ExceptionType.PluginInternalException, t);
            }

			ProtectedRegion newRegion;

			if (oldRegion instanceof ProtectedCuboidRegion) {
				newRegion = new ProtectedCuboidRegion(newRegionName, oldRegion.getMinimumPoint(), oldRegion.getMaximumPoint());
            } else {
                newRegion = new ProtectedPolygonalRegion(newRegionName, oldRegion.getPoints(), oldRegion.getMinimumPoint().getBlockY(), oldRegion.getMaximumPoint().getBlockY());
            }

            if (newRegion == null) {
                throw new ConfigRuntimeException("Error while redefining protected region", ExceptionType.PluginInternalException, t);
            }

			mgr.addRegion(newRegion);

			newRegion.setMembers(oldRegion.getMembers());
			newRegion.setOwners(oldRegion.getOwners());
			newRegion.setFlags(oldRegion.getFlags());
			newRegion.setPriority(oldRegion.getPriority());
			try {
				newRegion.setParent(oldRegion.getParent());
			} catch (ProtectedRegion.CircularInheritanceException ignore) {
			}

			mgr.removeRegion(oldRegionName);

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while renaming protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

    @api
    public static class sk_region_remove extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_remove";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

		@Override
        public String docs() {
            return "void {[world], name} Remove existed region.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            String region;

            if (args.length == 1) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }
            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

            mgr.removeRegion(region);

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while removing protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

    @api
    public static class sk_region_exists extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_exists";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

		@Override
        public String docs() {
            return "void {[world], name} Check if a given region exists.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            String region;

            if (args.length == 1) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }
            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists != null) {
                return CBoolean.TRUE;
            }

            return CBoolean.FALSE;
        }
    }

    @api
    public static class sk_region_addowner extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_addowner";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

		@Override
        public String docs() {
            return "void {region, [world], [owner1] | region, [world], [array(owner1, ownerN, ...)]} Add owner(s) to given region.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            MCPlayer m = null;
            String[] owners = null;
            String region = args[0].val();

            if (args.length == 1) {

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
					owners = new String[1];
					owners[0] = m.getName();
                }

            } else if (args.length == 2) {

                world = Bukkit.getServer().getWorld(args[0].val());

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
					owners = new String[1];
					owners[0] = m.getName();
                }

            } else {

                world = Bukkit.getServer().getWorld(args[1].val());

                if (args[2] instanceof CArray) {

                    CArray arg = (CArray) args[2];
					owners = new String[(int)arg.size()];

                    for (int i = 0; i < arg.size(); i++) {
						owners[i] = arg.get(i, t).val();
                    }
                } else {
					owners = new String[1];
					owners[0] = args[2].val();
                }

            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists == null) {
				if ("__global__".equalsIgnoreCase(region)) {
					regionExists = new GlobalProtectedRegion(region);
					mgr.addRegion(regionExists);
				} else {
					throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
				}
			}

			for (String owner : owners) {
				regionExists.getOwners().addPlayer(owner);
			}

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while adding owner(s) to protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

    @api
    public static class sk_region_remowner extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_remowner";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

		@Override
        public String docs() {
            return "void {region, [world], [owner1] | region, [world], [array(owner1, ownerN, ...)]} Remove owner(s) from given region.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            MCPlayer m = null;
			String[] owners = null;
            String region = args[0].val();

            if (args.length == 1) {

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
					owners = new String[1];
					owners[0] = m.getName();
                }

            } else if (args.length == 2) {

                world = Bukkit.getServer().getWorld(args[0].val());

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
					owners = new String[1];
					owners[0] = m.getName();
                }

            } else {

                world = Bukkit.getServer().getWorld(args[1].val());

                if (args[2] instanceof CArray) {

                    CArray arg = (CArray) args[2];
					owners = new String[(int)arg.size()];

                    for (int i = 0; i < arg.size(); i++) {
						owners[i] = arg.get(i, t).val();
                    }
                } else {
					owners = new String[1];
					owners[0] = args[2].val();
                }

            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

			for (String owner : owners) {
				regionExists.getOwners().removePlayer(owner);
			}

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while deleting owner(s) from protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

	@api
	public static class sk_region_owners extends SKFunction {

		@Override
		public String getName() {
			return "sk_region_owners";
		}

		@Override
        public Version since() {
            return CHVersion.V3_2_0;
        }

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {region, world} Returns an array of owners of this region. If the world"
					+ " or region cannot be found, a PluginInternalException is thrown.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PluginInternalException};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			Static.checkPlugin("WorldGuard", t);
			String regionName = args[0].val();
			String worldName = args[1].val();
			List<UUID> ownersPlayers = new ArrayList<>();
			List<String> ownersGroups = new ArrayList<>();
			World world = Bukkit.getServer().getWorld(worldName);
			if (world == null) {
				throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
			}
			RegionManager mgr = WorldGuardPlugin.inst().getRegionManager(world);
			ProtectedRegion region = mgr.getRegion(regionName);
			if (region == null) {
				throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, t);
			}

			ownersPlayers.addAll(region.getOwners().getUniqueIds());
			ownersGroups.addAll(region.getOwners().getGroups());

			CArray owners = CArray.GetAssociativeArray(t);
			CArray players = new CArray(t);
			CArray groups = new CArray(t);
			for (UUID owner : ownersPlayers) {
				players.push(new CString(owner.toString(), t));
			}
			for (String owner : ownersGroups) {
				groups.push(new CString(owner, t));
			}
			owners.set("players", players, t);
			owners.set("groups", groups, t);
			return owners;
		}
	}

    @api
    public static class sk_region_addmember extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_addmember";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

		@Override
        public String docs() {
            return "void {region, [world], [member1] | region, [world], [array(member1, memberN, ...)]} Add member(s) to given region.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            MCPlayer m = null;
			String[] members = null;
            String region = args[0].val();

            if (args.length == 1) {

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
					members = new String[1];
					members[0] = m.getName();
                }

            } else if (args.length == 2) {

                world = Bukkit.getServer().getWorld(args[0].val());

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
					members = new String[1];
					members[0] = m.getName();
                }

            } else {

                world = Bukkit.getServer().getWorld(args[1].val());

                if (args[2] instanceof CArray) {

                    CArray arg = (CArray) args[2];
					members = new String[(int)arg.size()];

                    for (int i = 0; i < arg.size(); i++) {
						members[i] = arg.get(i, t).val();
                    }
                } else {
					members = new String[1];
					members[0] = args[2].val();
                }

            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists == null) {
				if ("__global__".equalsIgnoreCase(region)) {
					regionExists = new GlobalProtectedRegion(region);
					mgr.addRegion(regionExists);
				} else {
					throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
				}
			}

			for (String member : members) {
				regionExists.getMembers().addPlayer(member);
			}

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while adding member(s) to protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

    @api
    public static class sk_region_remmember extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_remmember";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{1, 2, 3};
        }

		@Override
        public String docs() {
            return "void {region, [world], [member1] | region, [world], [array(member1, memberN, ...)]} Remove member(s) from given region.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
            MCPlayer m = null;
			String[] members = null;
            String region = args[0].val();

            if (args.length == 1) {

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
					members = new String[1];
					members[0] = m.getName();
                }

            } else if (args.length == 2) {

                world = Bukkit.getServer().getWorld(args[0].val());

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
					members = new String[1];
					members[0] = m.getName();
                }

            } else {

                world = Bukkit.getServer().getWorld(args[1].val());

                if (args[2] instanceof CArray) {

                    CArray arg = (CArray) args[2];
					members = new String[(int)arg.size()];

                    for (int i = 0; i < arg.size(); i++) {
						members[i] = arg.get(i, t).val();
                    }
                } else {
					members = new String[1];
					members[0] = args[2].val();
                }

            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
                throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

			for (String member : members) {
				regionExists.getMembers().removePlayer(member);
			}

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while deleting members(s) from protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

	@api
	public static class sk_region_members extends SKFunction {

		@Override
		public String getName() {
			return "sk_region_members";
		}

		@Override
        public Version since() {
            return CHVersion.V3_2_0;
        }

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {region, world} Returns an array of members of this region. If the world"
					+ " or region cannot be found, a PluginInternalException is thrown.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.PluginInternalException};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			Static.checkPlugin("WorldGuard", t);
			String regionName = args[0].val();
			String worldName = args[1].val();
			List<UUID> membersPlayers = new ArrayList<>();
			List<String> membersGroups = new ArrayList<>();
			World world = Bukkit.getServer().getWorld(worldName);
			if (world == null) {
				throw new ConfigRuntimeException("Unknown world specified", ExceptionType.PluginInternalException, t);
			}
			RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);
			ProtectedRegion region = mgr.getRegion(regionName);
			if (region == null) {
				throw new ConfigRuntimeException("Region could not be found!", ExceptionType.PluginInternalException, t);
			}

			membersPlayers.addAll(region.getMembers().getUniqueIds());
			membersGroups.addAll(region.getMembers().getGroups());

			CArray members = CArray.GetAssociativeArray(t);
			CArray players = new CArray(t);
			CArray groups = new CArray(t);
			for (UUID member : membersPlayers) {
				players.push(new CString(member.toString(), t));
			}
			for (String member : membersGroups) {
				groups.push(new CString(member, t));
			}
			members.set("players", players, t);
			members.set("groups", groups, t);
			return members;
		}
	}

    @api
    public static class sk_region_flag extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_flag";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{4, 5};
        }

		@Override
        public String docs() {
            return "void {world, region, flagName, flagValue, [group]} Add/change/remove flag for selected region. FlagName should be any"
					+ " supported flag from [http://wiki.sk89q.com/wiki/WorldGuard/Regions/Flags this list]. For the flagValue, use types which"
					+ " are supported by WorldGuard. Add group argument if you want to add WorldGuard group flag (read more about group"
					+ " flag types [http://wiki.sk89q.com/wiki/WorldGuard/Regions/Flags#Group here]). Set flagValue as null (and don't set"
					+ " group) to delete the flag from the region.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = Bukkit.getServer().getWorld(args[0].val());

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

			String regionName = args[1].val();
            String flagName = args[2].val();
			String flagValue = null;
			RegionGroup groupValue = null;

			if (args.length >= 4 && !(args[3] instanceof CNull) && !"".equals(args[3].val())) {
				flagValue = args[3].val();
			}

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion region = mgr.getRegion(regionName);

			if (region == null) {
				if ("__global__".equalsIgnoreCase(regionName)) {
					region = new GlobalProtectedRegion(regionName);
					mgr.addRegion(region);
				} else {
					throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", regionName, world.getName()), ExceptionType.PluginInternalException, t);
				}
			}

			Flag<?> foundFlag = null;

			for (Flag<?> flag : DefaultFlag.getFlags()) {
				if (flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
					foundFlag = flag;
					break;
				}
			}

			if (foundFlag == null) {
				throw new ConfigRuntimeException(String.format("Unknown flag specified: (%s).", flagName), ExceptionType.PluginInternalException, t);
			}

			if (args.length == 5) {
				String group = args[4].val();
				RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
				if (groupFlag == null) {
					throw new ConfigRuntimeException(String.format("Region flag (%s) does not have a group flag.", flagName), ExceptionType.PluginInternalException, t);
				}

				try {
					groupValue = groupFlag.parseInput(SKHandler.getWorldGuardPlugin(t), new BukkitMCCommandSender(env.getEnv(CommandHelperEnvironment.class).GetCommandSender())._CommandSender(), group);
				} catch (InvalidFlagFormat e) {
					throw new ConfigRuntimeException(String.format("Unknown group (%s).", group), ExceptionType.PluginInternalException, t);
				}

			}

			if (flagValue != null) {
				try {
					setFlag(t, region, foundFlag, new BukkitMCCommandSender(env.getEnv(CommandHelperEnvironment.class).GetCommandSender())._CommandSender(), flagValue);

				} catch (InvalidEnvironmentException e) {
					throw new ConfigRuntimeException(e.getMessage(), ExceptionType.PluginInternalException, t);
				} catch (InvalidFlagFormat e) {
					throw new ConfigRuntimeException(String.format("Unknown flag value specified: (%s).", flagValue), ExceptionType.PluginInternalException, t);
				}
			} else if (args.length < 5) {
				region.setFlag(foundFlag, null);

				RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
				if (groupFlag != null) {
					region.setFlag(groupFlag, null);
				}
			}

			if (groupValue != null) {
				RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();

				if (groupValue == groupFlag.getDefault()) {
					region.setFlag(groupFlag, null);
				} else {
					region.setFlag(groupFlag, groupValue);
				}
			}

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while defining flags", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }

		private <V> void setFlag(Target t, ProtectedRegion region,
				Flag<V> flag, CommandSender sender, String value)
						throws InvalidFlagFormat {
			region.setFlag(flag, flag.parseInput(SKHandler.getWorldGuardPlugin(t), sender, value));
		}
    }

    @api
	public static class sk_region_check_flag extends SKFunction {

		@Override
		public String getName() {
			return "sk_region_check_flag";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "mixed {locationArray, flagName, [player]} Check state of selected flag in defined location."
					+ " FlagName should be any supported flag from [http://wiki.sk89q.com/wiki/WorldGuard/Regions/Flags this list]."
					+ " Player defaults to the current player.";
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.FormatException,
				ExceptionType.PluginInternalException, ExceptionType.NotFoundException};
		}

		@Override
		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			Static.checkPlugin("WorldGuard", t);

			if ("build".equalsIgnoreCase(args[1].val())) {
				throw new ConfigRuntimeException(String.format("Can't use build flag with %s method. This is an limitation of WorldGuard.", this.getName()), ExceptionType.PluginInternalException, t);
			}

			MCPlayer p = null;
			MCWorld w = null;

			if (args.length == 2) {
				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}
			} else {
				p = Static.GetPlayer(args[2].val(), t);
			}

			if (p != null) {
				w = p.getWorld();
			}

			MCLocation l = ObjectGenerator.GetGenerator().location(args[0], w, t);

			Flag<?> foundFlag = null;

			for (Flag<?> flag : DefaultFlag.getFlags()) {
				if (flag.getName().replace("-", "").equalsIgnoreCase(args[1].val().replace("-", ""))) {
					foundFlag = flag;
					break;
				}
			}

			if (foundFlag == null) {
				throw new ConfigRuntimeException(String.format("Unknown flag specified: (%s).", args[1].val()), ExceptionType.PluginInternalException, t);
			}

			RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(Bukkit.getServer().getWorld(l.getWorld().getName()));

			ApplicableRegionSet set = mgr.getApplicableRegions(((BukkitMCLocation) l)._Location());

			if (foundFlag instanceof StateFlag) {
				if (p == null) {
					return CBoolean.get(set.allows((StateFlag) foundFlag));
				} else {
					return CBoolean.get(set.allows((StateFlag) foundFlag, SKHandler.getWorldGuardPlugin(t).wrapPlayer(((BukkitMCPlayer) p)._Player())));
				}
			} else {
				Object getFlag;

				if (p == null) {
					getFlag = set.getFlag((Flag) foundFlag);
				} else {
					getFlag = set.getFlag((Flag) foundFlag, SKHandler.getWorldGuardPlugin(t).wrapPlayer(((BukkitMCPlayer) p)._Player()));
				}

				if (foundFlag instanceof BooleanFlag) {
					Boolean value = ((BooleanFlag) foundFlag).unmarshal(getFlag);
					if (value != null) {
						return CBoolean.get(value);
					} else {
						return CNull.NULL;
					}
				} else if (foundFlag instanceof DoubleFlag) {
					Double value = ((DoubleFlag) foundFlag).unmarshal(getFlag);
					if (value != null) {
						return new CDouble(value, t);
					} else {
						return CNull.NULL;
					}
				} else if (foundFlag instanceof EnumFlag) {
					String value = ((EnumFlag) foundFlag).unmarshal(getFlag).name();
					if (value != null) {
						return new CString(value, t);
					} else {
						return CNull.NULL;
					}
				} else if (foundFlag instanceof IntegerFlag) {
					Integer value = ((IntegerFlag) foundFlag).unmarshal(getFlag);
					if (value != null) {
						return new CInt(value, t);
					} else {
						return CNull.NULL;
					}
				} else if (foundFlag instanceof LocationFlag) {
					com.sk89q.worldedit.Location value = ((LocationFlag) foundFlag).unmarshal(getFlag);
					if (value != null) {
						return new CArray(t,
								new CDouble(value.getPosition().getX(), t),
								new CDouble(value.getPosition().getY(), t),
								new CDouble(value.getPosition().getZ(), t),
								new CString(l.getWorld().getName(), t));
					} else {
						return CNull.NULL;
					}
				} else if (foundFlag instanceof RegionGroupFlag) {
					String value = ((RegionGroupFlag) foundFlag).unmarshal(getFlag).name();
					if (value != null) {
						return new CString(value, t);
					} else {
						return CNull.NULL;
					}
				} else if (foundFlag instanceof SetFlag) {

					CArray values = new CArray(t);

					Set setValue = ((SetFlag) foundFlag).unmarshal(getFlag);

					if (setValue != null) {
						for (Object setFlag : setValue) {
							if (setFlag instanceof String) {
								String value = (String) setFlag;
								values.push(new CString(value, t));
							} else if (setFlag instanceof EntityType) {
								String value = ((EntityType) setFlag).getName();
								values.push(new CString(value, t));
							} else {
								ConfigRuntimeException.DoWarning("One of the element of flag has unknown type. This is a developer mistake, please file a ticket.");
							}
						}
					}

					return values;

				} else if (foundFlag instanceof StringFlag) {
					String value = ((StringFlag) foundFlag).unmarshal(getFlag);
					if (value != null) {
						return new CString(value, t);
					} else {
						return CNull.NULL;
					}
				}

				throw new ConfigRuntimeException("The flag type is unknown. This is a developer mistake, please file a ticket.", ExceptionType.NotFoundException, t);
			}

		}
	}

    @api
    public static class sk_region_setpriority extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_setpriority";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

		@Override
        public String docs() {
            return "void {[world], region, priority} Sets priority for a given region.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

            World world = null;
			String region;
			int priority;

            if (args.length == 2) {

                region = args[0].val();

                MCPlayer m = null;

                if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
                    m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
                }

                if (m != null) {
                    world = Bukkit.getServer().getWorld(m.getWorld().getName());
                }

				priority = Static.getInt32(args[1], t);

            } else {
                region = args[1].val();
                world = Bukkit.getServer().getWorld(args[0].val());

				priority = Static.getInt32(args[2], t);
            }

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

			if ("__global__".equalsIgnoreCase(region)) {
				throw new ConfigRuntimeException("The region cannot be named __global__.", ExceptionType.PluginInternalException, t);
			}

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion regionExists = mgr.getRegion(region);

            if (regionExists == null) {
				throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), ExceptionType.PluginInternalException, t);
            }

			regionExists.setPriority(priority);

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while setting priority for protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

    @api
    public static class sk_region_setparent extends SKFunction {

		@Override
        public String getName() {
            return "sk_region_setparent";
        }

		@Override
        public Integer[] numArgs() {
            return new Integer[]{2, 3};
        }

		@Override
        public String docs() {
            return "void {world, region, [parentRegion]} Sets parent region for a given region.";
        }

		@Override
        public ExceptionType[] thrown() {
            return new ExceptionType[]{ExceptionType.InvalidWorldException, ExceptionType.PluginInternalException};
        }

		@Override
        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.checkPlugin("WorldGuard", t);

			String regionName;
			String parentName;


            World world = Bukkit.getServer().getWorld(args[0].val());

            if (world == null) {
                throw new ConfigRuntimeException("Unknown world specified", ExceptionType.InvalidWorldException, t);
            }

			regionName = args[1].val();

			if ("__global__".equalsIgnoreCase(regionName)) {
				throw new ConfigRuntimeException("You cannot set parents for a __global__ cuboid.", ExceptionType.PluginInternalException, t);
			}

            RegionManager mgr = SKHandler.getWorldGuardPlugin(t).getRegionManager(world);

            ProtectedRegion child = mgr.getRegion(regionName);

            if (child == null) {
				throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", regionName, world.getName()), ExceptionType.PluginInternalException, t);
            }

			if (args.length == 2) {
				try {
					child.setParent(null);
				} catch (ProtectedRegion.CircularInheritanceException ignore) {
				}
			} else {
				parentName = args[2].val();
				ProtectedRegion parent = mgr.getRegion(parentName);

				if (parent == null) {
					throw new ConfigRuntimeException(String.format("The region (%s) does not exist in world (%s).", parentName, world.getName()), ExceptionType.PluginInternalException, t);
				}

				try {
					child.setParent(parent);
				} catch (ProtectedRegion.CircularInheritanceException e) {
					throw new ConfigRuntimeException(String.format("Circular inheritance detected."), ExceptionType.PluginInternalException, t);
				}
			}

			try {
				mgr.save();
			} catch (StorageException e) {
				throw new ConfigRuntimeException("Error while setting parent for protected region", ExceptionType.PluginInternalException, t, e);
			}

            return CVoid.VOID;
        }
    }

	@api(environments=CommandHelperEnvironment.class)
	public static class sk_can_build extends SKFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PlayerOfflineException,
					ExceptionType.FormatException, ExceptionType.InvalidWorldException};
		}

		@Override
		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			Static.checkPlugin("WorldGuard", t);
			MCPlayer p;
			MCLocation loc;
			if (args.length == 1) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				if (p == null) {
					throw new ConfigRuntimeException("A player was expected.", ExceptionType.PlayerOfflineException, t);
				}
				loc = ObjectGenerator.GetGenerator().location(args[0], p.getWorld(), t);
			} else {

				p = Static.GetPlayer(args[0], t);
				loc = ObjectGenerator.GetGenerator().location(args[1], p.getWorld(), t);
			}
			return CBoolean.get(SKHandler.getWorldGuardPlugin(t).canBuild(((BukkitMCPlayer) p)._Player(),
					((BukkitMCLocation) loc)._Location()));
		}

		@Override
		public String getName() {
			return "sk_can_build";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "boolean {[player,] locationArray} Returns whether or not player can build at the location,"
					+ " according to WorldGuard. If player is not given, the current player is used.";
		}
	}
}
