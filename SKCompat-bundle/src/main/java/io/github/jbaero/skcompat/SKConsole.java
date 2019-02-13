package io.github.jbaero.skcompat;

import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.core.Static;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * @author jb_aero
 */
public class SKConsole extends SKCommandSender {

	private static final UUID uuid = UUID.fromString("43337e14-6cdc-45fc-b136-efd95f17a366");
	private final MCConsoleCommandSender console;
	private MCLocation location;
	private LocalSession localSession;

	public SKConsole() {
		console = Static.getServer().getConsole();
		setLocation(Static.getServer().getWorlds().get(0).getSpawnLocation());
	}

	@Override
	public Location getLocation() {
		return new Location(getWorld(), location.getX(), location.getY(),
				location.getZ(), location.getYaw(), location.getPitch());
	}

	@Override
	public void setLocation(MCLocation loc) {
		location = loc;
	}

	@Override
	public boolean setLocation(Location loc) {
		location = new BukkitMCLocation(BukkitAdapter.adapt(loc));
		return true;
	}

	@Override
	public MCWorld getMCWorld() {
		return location == null ? null : location.getWorld();
	}

	@Override
	public World getWorld() {
		if (location != null) {
			for (org.bukkit.World w : Bukkit.getWorlds()) {
				if (w.getName().equals(location.getWorld().getName())) {
					return BukkitAdapter.adapt(w);
				}
			}
		}
		return BukkitAdapter.adapt(Bukkit.getWorlds().get(0));
	}

	@Override
	public BaseItemStack getItemInHand(HandSide handSide) {
		return null;
	}

	@Override
	public void giveItem(BaseItemStack baseItemStack) {
		// do nothing
	}

	public void setWorld(MCWorld w) {
		location = StaticLayer.GetLocation(w, location.getX(), location.getY(), location.getZ(),
				location.getYaw(), location.getPitch());
	}

	@Override
	public String getName() {
		return console.getName();
	}

	@Override
	public LocalSession getLocalSession() {
		if (localSession == null) {
			localSession = WorldEdit.getInstance().getSessionManager().get(this);
		}
		return localSession;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void printRaw(String string) {
		for (String part : string.split("\n")) {
			console.sendMessage(part);
		}
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public void setPosition(Vector3 vector, float f, float f1) {
		location.setX(vector.getX());
		location.setY(vector.getY());
		location.setZ(vector.getZ());
		location.setPitch(f);
		location.setYaw(f1);
	}
}
