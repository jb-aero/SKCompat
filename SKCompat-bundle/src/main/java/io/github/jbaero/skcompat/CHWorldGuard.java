/*
 * CHWorldGuard, MethodScript functions for interacting with WorldGuard
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
import com.laytonsmith.PureUtilities.Point3D;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.BukkitMCOfflinePlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidPluginException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.github.jbaero.skcompat.SKCompat.SKFunction;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class CHWorldGuard {

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
			return MSVersion.V3_2_0;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class, CRECastException.class, CRERangeException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			String regionName = args[0].val();
			MCWorld world = Static.getWorld(args[1], t);
			int index = -1;

			if (args.length == 3) {
				index = Static.getInt32(args[2], t);
			}

			int maxIndex = 5;
			if (index < -1 || index > maxIndex) {
				throw new CRERangeException(
						this.getName() + " expects the index to be between -1 and " + maxIndex, t);
			}

			SKRegion region = SKWorldGuard.GetRegion(world, regionName, t);

			CArray ret = new CArray(t);

			//Fill these data structures in with the information we need
			if (index == 0 || index == -1) {

				CArray pointSet = new CArray(t);
				for (MCLocation l : region.getBounds()) {
					CArray point = new CArray(t);
					point.push(new CInt(l.getBlockX(), t), t);
					point.push(new CInt(l.getBlockY(), t), t);
					point.push(new CInt(l.getBlockZ(), t), t);
					point.push(new CString(l.getWorld().getName(), t), t);
					pointSet.push(point, t);
				}

				ret.push(pointSet, t);
			}

			if (index == 1 || index == -1) {

				CArray ownerSet = CArray.GetAssociativeArray(t);
				CArray players = new CArray(t);
				CArray names = new CArray(t);
				CArray groups = new CArray(t);
				for (UUID member : region.getOwnerPlayers()) {
					players.push(new CString(member.toString(), t), t);
				}
				for (String name : region.getOwnerNames()) {
					names.push(new CString(name, t), t);
				}
				for (String member : region.getOwnerGroups()) {
					groups.push(new CString(member, t), t);
				}
				ownerSet.set("players", players, t);
				ownerSet.set("names", names, t);
				ownerSet.set("groups", groups, t);

				ret.push(ownerSet, t);
			}

			if (index == 2 || index == -1) {

				CArray memberSet = CArray.GetAssociativeArray(t);
				CArray players = new CArray(t);
				CArray names = new CArray(t);
				CArray groups = new CArray(t);
				for (UUID member : region.getMemberPlayers()) {
					players.push(new CString(member.toString(), t), t);
				}
				for (String name : region.getMemberNames()) {
					names.push(new CString(name, t), t);
				}
				for (String member : region.getMemberGroups()) {
					groups.push(new CString(member, t), t);
				}
				memberSet.set("players", players, t);
				memberSet.set("names", names, t);
				memberSet.set("groups", groups, t);
				ret.push(memberSet, t);
			}

			if (index == 3 || index == -1) {

				CArray flagSet = new CArray(t);
				for (Map.Entry<String, String> flag : region.getFlags().entrySet()) {
					CArray fl = new CArray(t,
							new CString(flag.getKey(), t),
							new CString(flag.getValue(), t));
					flagSet.push(fl, t);
				}

				ret.push(flagSet, t);
			}

			if (index == 4 || index == -1) {
				ret.push(new CInt(region.getPriority(), t), t);
			}

			if (index == 5 || index == -1) {
				ret.push(new CDouble(region.getVolume(), t), t);
			}

			if (ret.size() == 1) {
				return ret.get(0, t);
			}
			return ret;
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
			return MSVersion.V3_2_0;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		@Override
		public String docs() {
			return "boolean {world, region1, array(region2, [regionN...])} Returns true or false whether or not the specified regions overlap."
					+ " Third parameter may be a single region or an array of regions.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			String region1 = args[1].val();
			List<ProtectedRegion> checkRegions = new ArrayList<>();
			MCWorld world = Static.getServer().getWorld(args[0].val());
			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);
			if (args[2] instanceof CArray) {
				CArray arg = (CArray) args[2];
				for (int i = 0; i < arg.size(); i++) {
					ProtectedRegion region = mgr.getRegion(arg.get(i, t).val());
					if (region == null) {
						throw new CREPluginInternalException("Region " + arg.get(i, t).val()
								+ " could not be found!", t);
					}
					checkRegions.add(region);
				}
			} else {
				ProtectedRegion region = mgr.getRegion(args[2].val());
				if (region == null) {
					throw new CREPluginInternalException("Region " + args[2] + " could not be found!", t);
				}
				checkRegions.add(region);
			}

			ProtectedRegion region = mgr.getRegion(region1);
			if (region == null) {
				throw new CREPluginInternalException("Region could not be found!", t);
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
			return MSVersion.V3_2_0;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "array {world, first_region, [other_region(s)]} Returns an array of regions names which intersect with defined region."
					+ " You can pass a single region, an array of regions to verify or omit this parameter and all regions in selected world will be checked.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			String region1 = args[1].val();
			List<ProtectedRegion> checkRegions = new ArrayList<>();
			List<ProtectedRegion> getRegions;
			CArray listRegions = new CArray(t);

			MCWorld world = Static.getServer().getWorld(args[0].val());
			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			if (args.length == 2) {
				checkRegions.addAll(mgr.getRegions().values());
			} else {
				if (args[2] instanceof CArray) {
					CArray arg = (CArray) args[2];
					for (int i = 0; i < arg.size(); i++) {
						ProtectedRegion region = mgr.getRegion(arg.get(i, t).val());
						if (region == null) {
							throw new CREPluginInternalException(String.format("Region %s could not be found!", arg.get(i, t).val()), t);
						}
						checkRegions.add(region);
					}
				} else {
					ProtectedRegion region = mgr.getRegion(args[2].val());
					if (region == null) {
						throw new CREPluginInternalException(String.format("Region %s could not be found!", args[2]), t);
					}
					checkRegions.add(region);
				}
			}

			ProtectedRegion region = mgr.getRegion(region1);
			if (region == null) {
				throw new CREPluginInternalException(String.format("Region %s could not be found!", region1), t);
			}

			try {
				getRegions = region.getIntersectingRegions(checkRegions);

				if (!getRegions.isEmpty()) {
					for (ProtectedRegion r : getRegions) {
						if (args.length != 2 || !r.getId().equals(region.getId())) {
							listRegions.push(new CString(r.getId(), t), t);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			CArray arr = new CArray(t);
			if (args.length == 1) {
				MCWorld world = Static.getWorld(args[0], t);
				for (String region : SKWorldGuard.GetRegionManager(world, t).getRegions().keySet()) {
					arr.push(new CString(region, t), t);
				}
			} else {
				for (MCWorld world : Static.getServer().getWorlds()) {
					for (String region : SKWorldGuard.GetRegionManager(world, t).getRegions().keySet()) {
						arr.push(new CString(region, t), t);
					}
				}
			}
			return arr;
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class sk_current_regions extends SKFunction {

		@Override
		public String getName() {
			return "sk_current_regions";
		}

		@Override
		public Version since() {
			return MSVersion.V3_2_0;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCPlayer m = null;
			if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
				m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			}
			if (args.length == 1) {
				m = Static.GetPlayer(args[0].val(), t);
			}

			if (m == null) {
				throw new CREPlayerOfflineException(this.getName() + " needs a player", t);
			}

			CArray regions = new CArray(t);

			for (String region : SKWorldGuard.RegionsAt(m.getLocation())) {
				regions.push(new CString(region, t), t);
			}

			return regions;
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class sk_regions_at extends SKFunction {

		@Override
		public String getName() {
			return "sk_regions_at";
		}

		@Override
		public Version since() {
			return MSVersion.V3_2_0;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREPluginInternalException.class, CREInsufficientArgumentsException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if (!(args[0] instanceof CArray)) {
				throw new CRECastException(this.getName() + " needs a locationarray", t);
			}

			MCWorld w = null;
			MCCommandSender c = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			if (c instanceof MCPlayer) {
				w = ((MCPlayer) c).getWorld();
			}

			MCLocation loc = ObjectGenerator.GetGenerator().location(args[0], w, t);

			if (loc.getWorld() == null) {
				throw new CREInsufficientArgumentsException(this.getName() + " needs a world", t);
			}

			MCWorld world = Static.getServer().getWorld(loc.getWorld().getName());
			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);
			BlockVector3 pt = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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
				regions.push(new CString(region.getId(), t), t);
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
			return MSVersion.V3_2_0;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[1].val());
			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion region = mgr.getRegion(args[0].val());

			if (region == null) {
				throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", args[0].val(), args[1].val()), t);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld w = null;
			String region;
			CArray points;
			List<BlockVector3> vertices = new ArrayList<>();

			if ((args.length == 3) || (args.length == 2 && "__global__".equalsIgnoreCase(args[1].val()))) {

				w = Static.getServer().getWorld(args[0].val());
				region = args[1].val();

				if (w == null) {
					throw new CREInvalidWorldException("The specified world \"" + args[0].val()
							+ "\" is not a valid world.", t);
				}
			} else {
				region = args[0].val();
			}

			if (!"__global__".equalsIgnoreCase(region)) {

				if ((args.length == 1) || !(args[args.length - 1] instanceof CArray)) {
					throw new CREPluginInternalException("Pass an array of LocationArrays for a new region.", t);
				}

				points = (CArray) args[args.length - 1];

				if (points.isEmpty()) {
					throw new CREPluginInternalException("Expecting an array of LocationArrays but found none.", t);
				}

				for (int i = 0; i < points.size(); i++) {

					if (!(points.get(i, t) instanceof CArray)) {
						throw new CREPluginInternalException("LocationArrays must be arrays.", t);
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

						vertices.add(BlockVector3.at(x, y, z));
					}
				}

				if (vertices.isEmpty()) {
					throw new CREPluginInternalException("Expecting an array of LocationArrays but found no valid Location arrays.", t);
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
										throw new CREInvalidWorldException(String.format(
												"Conflicting worlds in LocationArrays. (%s) & (%s)",
												w.getName(), world.getName()), t);
									}
								}
							}
						}
					}
				}
			}

			if (w == null) {
				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					w = env.getEnv(CommandHelperEnvironment.class).GetPlayer().getWorld();
				}
			}

			if (w == null) {
				throw new CREInvalidWorldException("No world specified.", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(w, t);

			if (mgr.hasRegion(region)) {
				throw new CREPluginInternalException(String.format("The region (%s) already exists in world (%s),"
						+ " and cannot be created again.", region, w.getName()), t);
			}

			ProtectedRegion newRegion;
			if ("__global__".equalsIgnoreCase(region)) {
				newRegion = SKWorldGuard.CreateProtectedRegion(region);
			} else {
				if (vertices.size() == 2) {
					newRegion = SKWorldGuard.CreateProtectedRegion(region, vertices);
				} else {

					List<BlockVector2> pointsPoly = new ArrayList<>();
					int minY = 0;
					int maxY = 0;

					for (int i = 0; i < vertices.size(); i++) {

						BlockVector3 vector = vertices.get(i);

						int x = vector.getBlockX();
						int y = vector.getBlockY();
						int z = vector.getBlockZ();

						pointsPoly.add(BlockVector2.at(x, z));

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
					newRegion = SKWorldGuard.CreateProtectedRegion(region, pointsPoly, minY, maxY);
				}
			}

			mgr.addRegion(newRegion);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while creating protected region", t, e);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
			String region;

			if (args.length == 2) {

				region = args[0].val();

				MCPlayer m = null;

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					world = Static.getServer().getWorld(m.getWorld().getName());
				}
			} else {
				region = args[1].val();
				world = Static.getServer().getWorld(args[0].val());
			}

			if (world == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}

			if ("__global__".equalsIgnoreCase(region)) {
				throw new CREPluginInternalException("You cannot change position of __global__ region.", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion oldRegion = mgr.getRegion(region);

			if (oldRegion == null) {
				throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), t);
			}

			if (!(args[args.length - 1] instanceof CArray)) {
				throw new CREPluginInternalException("Pass an array of points to define a new region", t);
			}

			List<BlockVector3> points = new ArrayList<>();
			List<BlockVector2> points2D = new ArrayList<>();

			int minY = 0;
			int maxY = 0;

			CArray arg = (CArray) args[args.length - 1];

			for (int i = 0; i < arg.size(); i++) {
				Point3D vec = ObjectGenerator.GetGenerator().vector(arg.get(i, t), t);

				if (arg.size() == 2) {
					points.add(BlockVector3.at(vec.X(), vec.Y(), vec.Z()));
				} else {
					points2D.add(BlockVector2.at(vec.X(), vec.Z()));

					if (i == 0) {
						minY = maxY = (int) vec.Y();
					} else {
						if (vec.Y() < minY) {
							minY = (int) vec.Y();
						} else if (vec.Y() > maxY) {
							maxY = (int) vec.Y();
						}
					}
				}
			}

			ProtectedRegion newRegion;
			if (arg.size() == 2) {
				newRegion = SKWorldGuard.CreateProtectedRegion(region, points);
			} else {
				newRegion = SKWorldGuard.CreateProtectedRegion(region, points2D, minY, maxY);
			}

			mgr.addRegion(newRegion);

			newRegion.setMembers(oldRegion.getMembers());
			newRegion.setOwners(oldRegion.getOwners());
			newRegion.setFlags(oldRegion.getFlags());
			newRegion.setPriority(oldRegion.getPriority());
			try {
				newRegion.setParent(oldRegion.getParent());
			} catch (Exception ex) {
				// CircularInheritanceException won't happen because the settings will be the same
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while redefining protected region", t, e);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
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
					world = Static.getServer().getWorld(m.getWorld().getName());
				}
			} else {
				oldRegionName = args[1].val();
				newRegionName = args[2].val();
				world = Static.getServer().getWorld(args[0].val());
			}

			if (world == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}

			if ("__global__".equalsIgnoreCase(oldRegionName)) {
				throw new CREPluginInternalException("You cannot change name of __global__ region.", t);
			}

			if ("__global__".equalsIgnoreCase(newRegionName)) {
				throw new CREPluginInternalException("You cannot change name of any region to __global__.", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion oldRegion = mgr.getRegion(oldRegionName);

			if (oldRegion == null) {
				throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).",
						oldRegionName, world.getName()), t);
			}

			ProtectedRegion newRegion = SKWorldGuard.CreateProtectedRegion(newRegionName, oldRegion);
			mgr.addRegion(newRegion);

			newRegion.setMembers(oldRegion.getMembers());
			newRegion.setOwners(oldRegion.getOwners());
			newRegion.setFlags(oldRegion.getFlags());
			newRegion.setPriority(oldRegion.getPriority());
			try {
				newRegion.setParent(oldRegion.getParent());
			} catch (Exception ignore) {
				//ProtectedRegion.CircularInheritanceException
			}

			mgr.removeRegion(oldRegionName);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while renaming protected region", t, e);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
			String region;

			if (args.length == 1) {

				region = args[0].val();

				MCPlayer m = null;

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					world = Static.getServer().getWorld(m.getWorld().getName());
				}
			} else {
				region = args[1].val();
				world = Static.getServer().getWorld(args[0].val());
			}

			if (world == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists == null) {
				throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), t);
			}

			mgr.removeRegion(region);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while removing protected region", t, e);
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
			return "boolean {[world], name} Check if a given region exists.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
			String region;

			if (args.length == 1) {

				region = args[0].val();

				MCPlayer m = null;

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					world = Static.getServer().getWorld(m.getWorld().getName());
				}
			} else {
				region = args[1].val();
				world = Static.getServer().getWorld(args[0].val());
			}

			if (world == null) {
				throw new CREInvalidWorldException("Unknown world specified", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
			MCPlayer m = null;
			String[] owners = null;
			String region = args[0].val();

			if (args.length == 1) {

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					world = Static.getServer().getWorld(m.getWorld().getName());
					owners = new String[1];
					owners[0] = m.getName();
				}

			} else if (args.length == 2) {

				world = Static.getServer().getWorld(args[0].val());

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					owners = new String[1];
					owners[0] = m.getName();
				}

			} else {

				world = Static.getServer().getWorld(args[1].val());

				if (args[2] instanceof CArray) {

					CArray arg = (CArray) args[2];
					owners = new String[(int) arg.size()];

					for (int i = 0; i < arg.size(); i++) {
						owners[i] = arg.get(i, t).val();
					}
				} else {
					owners = new String[1];
					owners[0] = args[2].val();
				}

			}

			if (world == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists == null) {
				if ("__global__".equalsIgnoreCase(region)) {
					regionExists = SKWorldGuard.CreateProtectedRegion(region);
					mgr.addRegion(regionExists);
				} else {
					throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), t);
				}
			}

			if(owners == null) {
				throw new CREInsufficientArgumentsException("New owners not specified.", t);
			}

			for (String owner : owners) {
				OfflinePlayer o = (OfflinePlayer) ReflectionUtils.get(BukkitMCOfflinePlayer.class, Static.GetUser(owner, t), "op");
				regionExists.getOwners().addPlayer(o.getUniqueId());
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while adding owner(s) to protected region", t, e);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
			MCPlayer m = null;
			String[] owners = null;
			String region = args[0].val();

			if (args.length == 1) {

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					world = Static.getServer().getWorld(m.getWorld().getName());
					owners = new String[1];
					owners[0] = m.getName();
				}

			} else if (args.length == 2) {

				world = Static.getServer().getWorld(args[0].val());

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					owners = new String[1];
					owners[0] = m.getName();
				}

			} else {

				world = Static.getServer().getWorld(args[1].val());

				if (args[2] instanceof CArray) {

					CArray arg = (CArray) args[2];
					owners = new String[(int) arg.size()];

					for (int i = 0; i < arg.size(); i++) {
						owners[i] = arg.get(i, t).val();
					}
				} else {
					owners = new String[1];
					owners[0] = args[2].val();
				}

			}

			if (world == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists == null) {
				throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), t);
			}

			if(owners == null) {
				throw new CREInsufficientArgumentsException("Owners not specified.", t);
			}

			for (String owner : owners) {
				OfflinePlayer o = (OfflinePlayer) ReflectionUtils.get(BukkitMCOfflinePlayer.class, Static.GetUser(owner, t), "op");
				regionExists.getOwners().removePlayer(o.getUniqueId());
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while deleting owner(s) from protected region", t, e);
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
			return MSVersion.V3_2_0;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String regionName = args[0].val();
			String worldName = args[1].val();
			List<UUID> ownersPlayers = new ArrayList<>();
			List<String> ownersNames = new ArrayList<>();
			List<String> ownersGroups = new ArrayList<>();
			MCWorld world = Static.getServer().getWorld(worldName);
			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);
			ProtectedRegion region = mgr.getRegion(regionName);
			if (region == null) {
				throw new CREPluginInternalException("Region could not be found!", t);
			}

			ownersPlayers.addAll(region.getOwners().getUniqueIds());
			ownersNames.addAll(region.getOwners().getPlayers());
			ownersGroups.addAll(region.getOwners().getGroups());

			CArray owners = CArray.GetAssociativeArray(t);
			CArray players = new CArray(t);
			CArray names = new CArray(t);
			CArray groups = new CArray(t);
			for (UUID owner : ownersPlayers) {
				players.push(new CString(owner.toString(), t), t);
			}
			for (String owner : ownersNames) {
				names.push(new CString(owner, t), t);
			}
			for (String owner : ownersGroups) {
				groups.push(new CString(owner, t), t);
			}
			owners.set("players", players, t);
			owners.set("names", names, t);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
			MCPlayer m = null;
			String[] members = null;
			String region = args[0].val();

			if (args.length == 1) {

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					world = Static.getServer().getWorld(m.getWorld().getName());
					members = new String[1];
					members[0] = m.getName();
				}

			} else if (args.length == 2) {

				world = Static.getServer().getWorld(args[0].val());

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					members = new String[1];
					members[0] = m.getName();
				}

			} else {

				world = Static.getServer().getWorld(args[1].val());

				if (args[2] instanceof CArray) {

					CArray arg = (CArray) args[2];
					members = new String[(int) arg.size()];

					for (int i = 0; i < arg.size(); i++) {
						members[i] = arg.get(i, t).val();
					}
				} else {
					members = new String[1];
					members[0] = args[2].val();
				}

			}

			if (world == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists == null) {
				if ("__global__".equalsIgnoreCase(region)) {
					regionExists = SKWorldGuard.CreateProtectedRegion(region);
					mgr.addRegion(regionExists);
				} else {
					throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), t);
				}
			}

			if(members == null) {
				throw new CREInsufficientArgumentsException("Members not specified.", t);
			}

			for (String member : members) {
				OfflinePlayer o = (OfflinePlayer) ReflectionUtils.get(BukkitMCOfflinePlayer.class, Static.GetUser(member, t), "op");
				regionExists.getMembers().addPlayer(o.getUniqueId());
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while adding member(s) to protected region", t, e);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
			MCPlayer m = null;
			String[] members = null;
			String region = args[0].val();

			if (args.length == 1) {

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					world = Static.getServer().getWorld(m.getWorld().getName());
					members = new String[1];
					members[0] = m.getName();
				}

			} else if (args.length == 2) {

				world = Static.getServer().getWorld(args[0].val());

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					members = new String[1];
					members[0] = m.getName();
				}

			} else {

				world = Static.getServer().getWorld(args[1].val());

				if (args[2] instanceof CArray) {

					CArray arg = (CArray) args[2];
					members = new String[(int) arg.size()];

					for (int i = 0; i < arg.size(); i++) {
						members[i] = arg.get(i, t).val();
					}
				} else {
					members = new String[1];
					members[0] = args[2].val();
				}

			}

			if (world == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists == null) {
				throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), t);
			}

			if(members == null) {
				throw new CREInsufficientArgumentsException("Members not specified.", t);
			}

			for (String member : members) {
				OfflinePlayer o = (OfflinePlayer) ReflectionUtils.get(BukkitMCOfflinePlayer.class, Static.GetUser(member, t), "op");
				regionExists.getMembers().removePlayer(o.getUniqueId());
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while deleting members(s) from protected region", t, e);
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
			return MSVersion.V3_2_0;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String regionName = args[0].val();
			String worldName = args[1].val();
			List<UUID> membersPlayers = new ArrayList<>();
			List<String> membersNames = new ArrayList<>();
			List<String> membersGroups = new ArrayList<>();
			MCWorld world = Static.getServer().getWorld(worldName);
			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);
			ProtectedRegion region = mgr.getRegion(regionName);
			if (region == null) {
				throw new CREPluginInternalException("Region could not be found!", t);
			}

			membersPlayers.addAll(region.getMembers().getUniqueIds());
			membersNames.addAll(region.getMembers().getPlayers());
			membersGroups.addAll(region.getMembers().getGroups());

			CArray members = CArray.GetAssociativeArray(t);
			CArray players = new CArray(t);
			CArray names = new CArray(t);
			CArray groups = new CArray(t);
			for (UUID member : membersPlayers) {
				players.push(new CString(member.toString(), t), t);
			}
			for (String member : membersNames) {
				names.push(new CString(member, t), t);
			}
			for (String member : membersGroups) {
				groups.push(new CString(member, t), t);
			}
			members.set("players", players, t);
			members.set("names", names, t);
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
					+ " supported flag from [https://worldguard.readthedocs.io/en/latest/regions/flags/ this list]. For the flagValue, use types which"
					+ " are supported by WorldGuard. Add group argument if you want to add WorldGuard group flag (read more about group"
					+ " flag types [http://worldguard.enginehub.org/en/latest/regions/flags/#region-groups here]). Set flagValue as null (and don't set"
					+ " group) to delete the flag from the region.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = Static.getServer().getWorld(args[0].val());

			MCCommandSender sender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String regionName = args[1].val();
			String flagName = args[2].val();
			String flagValue = null;

			if (args.length >= 4 && !(args[3] instanceof CNull) && !"".equals(args[3].val())) {
				flagValue = args[3].val();
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			if (mgr.getRegion(regionName) == null) {
				if ("__global__".equalsIgnoreCase(regionName)) {
					SKWorldGuard.GetRegionManager(world, t).addRegion(SKWorldGuard.CreateProtectedRegion(regionName));
				} else {
					throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", regionName, world.getName()), t);
				}
			}

			SKRegion region = SKWorldGuard.GetRegion(world, regionName, t);
			if (flagValue != null) {
				region.setFlag(flagName, sender, flagValue, t);
			} else if (args.length < 5) {
				region.clearFlag(flagName, t);
				if (SKWorldGuard.GetFlag(flagName, t).getRegionGroupFlag() != null) {
					region.clearGroupFlag(flagName, t);
				}
			}

			if (args.length == 5) {
				String group = args[4].val();
				region.setGroupFlag(flagName, sender, group, t);
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while defining flags", t, e);
			}

			return CVoid.VOID;
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
					+ " FlagName should be any supported flag from [https://worldguard.readthedocs.io/en/latest/regions/flags/ this list]."
					+ " Player defaults to the current player.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREFormatException.class,
					CREPluginInternalException.class, CRENotFoundException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			if ("build".equalsIgnoreCase(args[1].val())) {
				throw new CREPluginInternalException(String.format("Can't use build flag with %s method. This is an limitation of WorldGuard.", this.getName()), t);
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

			Flag<?> foundFlag = SKWorldGuard.GetFlag(args[1].val(), t);
			RegionManager mgr = SKWorldGuard.GetRegionManager(l.getWorld(), t);

			ApplicableRegionSet set = mgr.getApplicableRegions(BukkitAdapter.asBlockVector((Location) l.getHandle()));
			if (foundFlag instanceof StateFlag) {
				return CBoolean.get(SKWorldGuard.TestState(set, p, (StateFlag) foundFlag));
			}

			Object getFlag = SKWorldGuard.QueryValue(set, p, (Flag<?>) foundFlag);

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
				return new CString(((EnumFlag<?>) foundFlag).unmarshal(getFlag).name(), t);
			} else if (foundFlag instanceof IntegerFlag) {
				Integer value = ((IntegerFlag) foundFlag).unmarshal(getFlag);
				if (value != null) {
					return new CInt(value, t);
				} else {
					return CNull.NULL;
				}
			} else if (foundFlag instanceof LocationFlag) {
				com.sk89q.worldedit.util.Location value = ((LocationFlag) foundFlag).unmarshal(getFlag);
				if (value != null) {
					return new CArray(t,
							new CDouble(value.getX(), t),
							new CDouble(value.getY(), t),
							new CDouble(value.getZ(), t),
							new CString(l.getWorld().getName(), t));
				} else {
					return CNull.NULL;
				}
			} else if (foundFlag instanceof SetFlag) {
				CArray values = new CArray(t);
				Set<?> setValue = ((SetFlag<?>) foundFlag).unmarshal(getFlag);
				if (setValue != null) {
					for (Object setFlag : setValue) {
						if (setFlag instanceof String) {
							String value = (String) setFlag;
							values.push(new CString(value, t), t);
						} else if (setFlag instanceof EntityType) {
							String value = ((EntityType) setFlag).getName();
							values.push(new CString(value, t), t);
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
			throw new CRENotFoundException("The flag type is unknown. This is a developer mistake, please file a ticket.", t);
		}
	}
	
	@api
	public static class sk_region_flags extends SKFunction {

		@Override
		public String getName() {
			return "sk_region_flags";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "array {region, world} Returns an associative array with the flags of the region "
					+ "in format: array(flag-name: flag-value, ...). If the world"
					+ " or region cannot be found, a PluginInternalException is thrown.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			String regionName = args[0].val();
			MCWorld world = Static.getWorld(args[1], t);
			
			SKRegion region = SKWorldGuard.GetRegion(world, regionName, t);
			
			CArray flagSet = new CArray(t);
			for (Map.Entry<String, String> flag : region.getFlags().entrySet()) {
				flagSet.set(flag.getKey(), flag.getValue(), t);
			}
			
			return flagSet;
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			MCWorld world = null;
			String region;
			int priority;

			if (args.length == 2) {

				region = args[0].val();

				MCPlayer m = null;

				if (env.getEnv(CommandHelperEnvironment.class).GetCommandSender() instanceof MCPlayer) {
					m = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
				}

				if (m != null) {
					world = Static.getServer().getWorld(m.getWorld().getName());
				}

				priority = Static.getInt32(args[1], t);

			} else {
				region = args[1].val();
				world = Static.getServer().getWorld(args[0].val());

				priority = Static.getInt32(args[2], t);
			}

			if (world == null) {
				throw new CREInvalidWorldException("No world specified", t);
			}

			if ("__global__".equalsIgnoreCase(region)) {
				throw new CREPluginInternalException("The region cannot be named __global__.", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion regionExists = mgr.getRegion(region);

			if (regionExists == null) {
				throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", region, world.getName()), t);
			}

			regionExists.setPriority(priority);

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while setting priority for protected region", t, e);
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidWorldException.class, CREPluginInternalException.class};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			String regionName;
			String parentName;

			MCWorld world = Static.getServer().getWorld(args[0].val());

			regionName = args[1].val();

			if ("__global__".equalsIgnoreCase(regionName)) {
				throw new CREPluginInternalException("You cannot set parents for a __global__ cuboid.", t);
			}

			RegionManager mgr = SKWorldGuard.GetRegionManager(world, t);

			ProtectedRegion child = mgr.getRegion(regionName);

			if (child == null) {
				throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", regionName, world.getName()), t);
			}

			if (args.length == 2) {
				try {
					child.setParent(null);
				} catch (Exception ignore) {
					// ProtectedRegion.CircularInheritance
				}
			} else {
				parentName = args[2].val();
				ProtectedRegion parent = mgr.getRegion(parentName);

				if (parent == null) {
					throw new CREPluginInternalException(String.format("The region (%s) does not exist in world (%s).", parentName, world.getName()), t);
				}

				try {
					child.setParent(parent);
				} catch (Exception e) {
					throw new CREPluginInternalException(e.getMessage(), t);
				}
			}

			try {
				mgr.save();
			} catch (Exception e) {
				throw new CREPluginInternalException("Error while setting parent for protected region", t, e);
			}

			return CVoid.VOID;
		}
	}

	@api(environments = CommandHelperEnvironment.class)
	public static class sk_can_build extends SKFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREInvalidPluginException.class, CREPlayerOfflineException.class,
					CREFormatException.class, CREInvalidWorldException.class};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			MCPlayer p;
			MCLocation loc;
			if (args.length == 1) {
				p = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
				if (p == null) {
					throw new CREPlayerOfflineException("A player was expected.", t);
				}
				loc = ObjectGenerator.GetGenerator().location(args[0], p.getWorld(), t);
			} else {
				p = Static.GetPlayer(args[0], t);
				loc = ObjectGenerator.GetGenerator().location(args[1], p.getWorld(), t);
			}
			LocalPlayer player = WorldGuardPlugin.inst().wrapPlayer((Player) p.getHandle());
			com.sk89q.worldedit.world.World world =  BukkitAdapter.adapt((World) loc.getWorld().getHandle());
			if(WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(player, world)) {
				return CBoolean.TRUE;
			}
			RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
			return CBoolean.get(query.testState(BukkitAdapter.adapt((Location) loc.getHandle()), player, Flags.BUILD));
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
