package io.github.jbaero.skcompat;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.InvalidEnvironmentException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SKRegion {
	World world;
	ProtectedRegion region;

	public SKRegion(World world, ProtectedRegion region) {
		this.world = world;
		this.region = region;
	}

	public Collection<MCLocation> getBounds() {
		List<MCLocation> points = new ArrayList<>();

		boolean first = true;
		if (this.region instanceof ProtectedPolygonalRegion) {
			for (BlockVector2 pt : this.region.getPoints()) {
				points.add(new BukkitMCLocation(new Location(world, pt.getX(),
						first ? this.region.getMaximumPoint().getY() : this.region.getMinimumPoint().getY(), pt.getZ())));
				first = false;
			}
		} else {
			points.add(new BukkitMCLocation(BukkitAdapter.adapt(world, this.region.getMaximumPoint())));
			points.add(new BukkitMCLocation(BukkitAdapter.adapt(world, this.region.getMinimumPoint())));
		}

		return points;
	}

	public Collection<UUID> getOwnerPlayers() {
		return this.region.getOwners().getUniqueIds();
	}

	public Collection<String> getOwnerNames() {
		return this.region.getOwners().getPlayers();
	}

	public Collection<String> getOwnerGroups() {
		return this.region.getOwners().getGroups();
	}

	public Collection<UUID> getMemberPlayers() {
		return this.region.getMembers().getUniqueIds();
	}

	public Collection<String> getMemberNames() {
		return this.region.getMembers().getPlayers();
	}

	public Collection<String> getMemberGroups() {
		return this.region.getMembers().getGroups();
	}

	public Map<String, String> getFlags() {

		Map<String, String> flags = new HashMap<>();
		for (Map.Entry<Flag<?>, Object> ent : this.region.getFlags().entrySet()) {
			flags.put(ent.getKey().getName(), String.valueOf(ent.getValue()));
		}

		return flags;
	}

	public int getPriority() {
		return this.region.getPriority();
	}

	public float getVolume() {
		return this.region.volume();
	}

	public void clearFlag(String flagName, Target t) {
		this.region.setFlag(SKWorldGuard.GetFlag(flagName, t), null);
	}

	public void clearGroupFlag(String flagName, Target t) {
		this.region.setFlag(SKWorldGuard.GetFlag(flagName, t).getRegionGroupFlag(), null);
	}

	public <V> void setFlag(String flagName, MCCommandSender sender, String value, Target t) {
		Flag<?> foundFlag = SKWorldGuard.GetFlag(flagName, t);
		setFlag(foundFlag, sender, value, t);
	}

	private <V> void setFlag(Flag<V> flag, MCCommandSender sender, String value, Target t) {
		try {
			this.region.setFlag(flag, flag.parseInput(FlagContext.create()
					.setSender(WorldGuardPlugin.inst().wrapCommandSender((CommandSender) sender.getHandle()))
					.setInput(value).build()));
		} catch (InvalidEnvironmentException e) {
			throw new CREPluginInternalException(e.getMessage(), t);
		} catch (InvalidFlagFormat e) {
			throw new CREPluginInternalException(String.format("Unknown flag value specified: (%s).", value), t);
		}
	}

	public void setGroupFlag(String flagName, MCCommandSender sender, String group, Target t) {
		Flag<?> flag = SKWorldGuard.GetFlag(flagName, t);
		RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
		if (groupFlag == null) {
			throw new CREPluginInternalException(String.format("Region flag (%s) does not have a group flag.", flag.getName()), t);
		}
		RegionGroup groupValue;
		try {
			groupValue = groupFlag.parseInput(FlagContext.create()
					.setSender(WorldGuardPlugin.inst().wrapCommandSender((CommandSender) sender.getHandle()))
					.setInput(group).build());
		} catch (InvalidFlagFormat e) {
			throw new CREPluginInternalException(String.format("Unknown group (%s).", group), t);
		}
		if (groupValue == groupFlag.getDefault()) {
			this.region.setFlag(groupFlag, null);
		} else {
			this.region.setFlag(groupFlag, groupValue);
		}
	}
}
