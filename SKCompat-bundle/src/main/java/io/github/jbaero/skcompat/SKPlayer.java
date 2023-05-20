package io.github.jbaero.skcompat;

import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import org.bukkit.entity.Player;

public class SKPlayer extends BukkitPlayer {

	public SKPlayer(Player player) {
		super(player);
	}

	@Override
	public boolean hasPermission(String perm) {
		return true;
	}

	@Override
	public void checkPermission(String permission) throws AuthorizationException {
	}

}
