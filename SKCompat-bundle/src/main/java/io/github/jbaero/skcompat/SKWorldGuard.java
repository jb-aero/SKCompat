package io.github.jbaero.skcompat;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SKWorldGuard {

	public static RegionManager GetRegionManager(MCWorld world, Target t) {
		RegionManager mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt((World) world.getHandle()));
		if(mgr == null) {
			throw new CREPluginInternalException("Could not find region manager for world: " + world.getName(), t);
		}
		return mgr;
	}

	public static SKRegion GetRegion(MCWorld w, String regionName, Target t) {
		World world = (World) w.getHandle();
		RegionManager mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
		if(mgr == null) {
			throw new CREPluginInternalException("Could not find region manager for world: " + w.getName(), t);
		}
		ProtectedRegion region = mgr.getRegion(regionName);
		if (region == null) {
			throw new CREPluginInternalException("Region could not be found!", t);
		}
		return new SKRegion(world, region);
	}

	public static Collection<String> RegionsAt(MCWorld world, int x, int y, int z) {
		RegionManager mgr = GetRegionManager(world, Target.UNKNOWN);
		BlockVector3 pt = BlockVector3.at(x, y, z);
		ApplicableRegionSet set = mgr.getApplicableRegions(pt);

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

		List<String> ret = new ArrayList<>();
		for (ProtectedRegion r : sortedRegions) {
			ret.add(r.getId());
		}

		return ret;
	}

	public static Flag<?> GetFlag(String flagName, Target t) {
		Flag<?> foundFlag = null;
		for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry().getAll()) {
			if (flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
				foundFlag = flag;
				break;
			}
		}
		if (foundFlag == null) {
			throw new CREPluginInternalException(String.format("Unknown flag specified: (%s).", flagName), t);
		}
		return foundFlag;
	}
	
	public static boolean TestState(ApplicableRegionSet set, MCPlayer p, StateFlag flag) {
		if(p == null) {
			return set.testState(null, flag);
		} else {
			return set.testState(WorldGuardPlugin.inst().wrapPlayer((Player) p.getHandle()), flag);
		}
	}

	public static <V> V QueryValue(ApplicableRegionSet set, MCPlayer p, Flag<V> flag) {
		if(p == null) {
			return set.queryValue(null, flag);
		} else {
			return set.queryValue(WorldGuardPlugin.inst().wrapPlayer((Player) p.getHandle()), flag);
		}
	}

	public static Collection<String> RegionsAt(MCLocation loc) {
		return RegionsAt(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public static ProtectedRegion CreateProtectedRegion(String region) {
		return new GlobalProtectedRegion(region);
	}

	public static ProtectedRegion CreateProtectedRegion(String newRegionName, ProtectedRegion oldRegion) {
		if (oldRegion instanceof ProtectedCuboidRegion) {
			return new ProtectedCuboidRegion(newRegionName, oldRegion.getMinimumPoint(), oldRegion.getMaximumPoint());
		} else {
			return new ProtectedPolygonalRegion(newRegionName, oldRegion.getPoints(),
					oldRegion.getMinimumPoint().getBlockY(), oldRegion.getMaximumPoint().getBlockY());
		}
	}

	public static ProtectedRegion CreateProtectedRegion(String region, List<BlockVector3> points) {
		return new ProtectedCuboidRegion(region, points.get(0), points.get(1));
	}

	public static ProtectedRegion CreateProtectedRegion(String region, List<BlockVector2> points2D, int minY, int maxY) {
		return new ProtectedPolygonalRegion(region, points2D, minY, maxY);
	}
}
