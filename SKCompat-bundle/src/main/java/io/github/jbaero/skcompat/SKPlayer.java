package io.github.jbaero.skcompat;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

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
		String worldName = player.getWorld().getName();
		for (org.bukkit.World w : Bukkit.getWorlds()) {
			if (w.getName().equals(worldName)) {
				return BukkitAdapter.adapt(w);
			}
		}
		return BukkitAdapter.adapt(Bukkit.getWorlds().get(0));
	}

	@Override
	public BaseItemStack getItemInHand(HandSide handSide) {
		if(handSide == HandSide.MAIN_HAND) {
			return BukkitAdapter.adapt((ItemStack) player.getItemInHand().getHandle());
		} else {
			return BukkitAdapter.adapt((ItemStack) player.getInventory().getItemInOffHand().getHandle());
		}
	}

	@Override
	public void giveItem(BaseItemStack baseItemStack) {
		player.getInventory().addItem(new BukkitMCItemStack(BukkitAdapter.adapt(baseItemStack)));
	}

	@Override
	public LocalSession getLocalSession() {
		return WorldEdit.getInstance().getSessionManager().get(this);
	}

	@Override
	public void setPosition(Vector3 vector, float v, float v1) {
		player.teleport(StaticLayer.GetLocation(player.getWorld(), vector.getX(), vector.getY(), vector.getZ(), v, v1));
	}

	@Override
	public String getName() {
		return player.getName();
	}

	@Override
	public boolean isActive() {
		return player.isOnline();
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

	@Override
	public boolean setLocation(Location loc) {
		return player.teleport(new BukkitMCLocation(BukkitAdapter.adapt(loc)));
	}

	@Override
	public void dispatchCUIEvent(CUIEvent event) {
		BukkitAdapter.adapt((org.bukkit.entity.Player) player.getHandle()).dispatchCUIEvent(event);
	}
}
