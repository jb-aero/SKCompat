package io.github.jbaero.skcompat;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;

import java.util.UUID;

/**
 * @author jb_aero
 */
public class SKPlayer extends SKCommandSender {

	MCPlayer player;

	public SKPlayer(MCPlayer player) {
		this.player = player;
	}

	@Override
	public World getWorld() {
		for (World w : WorldEdit.getInstance().getServer().getWorlds()) {
			if (w.getName().equals(player.getWorld().getName())) {
				return w;
			}
		}
		return WorldEdit.getInstance().getServer().getWorlds().get(0);
	}

	@Override
	public MCWorld getMCWorld() {
		return player.getWorld();
	}

	@Override
	public int getItemInHand() {
		return player.getItemInHand() == null ? 0 : player.getItemInHand().getTypeId();
	}

	@Override
	public LocalSession getLocalSession() {
		return WorldEdit.getInstance().getSessionManager().get(this);
	}

	@Override
	public double getPitch() {
		return player.getLocation().getPitch();
	}

	@Override
	public double getYaw() {
		return player.getLocation().getYaw();
	}

	@Override
	public void setPosition(Vector vector, float v, float v1) {
		player.teleport(StaticLayer.GetLocation(player.getWorld(), vector.getX(), vector.getY(), vector.getZ(), v, v1));
	}

	@Override
	public String getName() {
		return player.getName();
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public void printRaw(String s) {
		for (String part : s.split("\n")) {
			player.sendMessage(part);
		}
	}

	@Override
	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	@Override
	public Location getLocation() {
		return new Location(getWorld(), player.getLocation().getX(), player.getLocation().getY(),
				player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
	}

	@Override
	public void setLocation(MCLocation loc) {
		player.teleport(loc);
	}
}
