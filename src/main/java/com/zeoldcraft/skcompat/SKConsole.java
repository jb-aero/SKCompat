package com.zeoldcraft.skcompat;

import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.Static;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;

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
	public MCWorld getMCWorld() {
		return location == null ? null : location.getWorld();
	}

	@Override
	public World getWorld() {
		if (location != null) {
			for (World w : WorldEdit.getInstance().getServer().getWorlds()) {
				if (w.getName().equals(location.getWorld().getName())) {
					return w;
				}
			}
		}
		World ret = WorldEdit.getInstance().getServer().getWorlds().get(0);
		return ret;
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
	public int getItemInHand() {
		return 0;
	}

	@Override
	public double getPitch() {
		return location.getPitch();
	}

	@Override
	public double getYaw() {
		return location.getY();
	}

	@Override
	public void setPosition(Vector vector, float f, float f1) {
		location.setX(vector.getX());
		location.setY(vector.getY());
		location.setZ(vector.getZ());
		location.setPitch(f);
		location.setYaw(f1);
	}
}
