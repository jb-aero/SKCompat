package io.github.jbaero.skcompat;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.AbstractNonPlayerActor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

public class SKConsole extends AbstractNonPlayerActor implements Locatable {

	private static final UUID uuid = UUID.fromString("43337e14-6cdc-45fc-b136-efd95f17a366");
	private Location location;

	public SKConsole() {
		location = BukkitAdapter.adapt(Bukkit.getWorlds().get(0).getSpawnLocation());
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public boolean setLocation(Location loc) {
		location = loc;
		return true;
	}

	@Override
	public Extent getExtent() {
		return location.getExtent();
	}

	@Override
	public String getName() {
		return Bukkit.getConsoleSender().getName();
	}

	@Override
	public void printRaw(String string) {
	}

	@Override
	public void printDebug(String msg) {
	}

	@Override
	public void print(String msg) {
	}

	@Override
	public void printError(String msg) {
	}

	@Override
	public void print(Component component) {
		// Do nothing
	}

	@Override
	public Locale getLocale() {
		return WorldEdit.getInstance().getConfiguration().defaultLocale;
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public String[] getGroups() {
		return new String[0];
	}

	@Override
	public void checkPermission(String permission) throws AuthorizationException {
		// console has all permissions
	}

	@Override
	public boolean hasPermission(String permission) {
		return true;
	}

	@Override
	public SessionKey getSessionKey() {
		return new SessionKey() {
			@Nullable
			@Override
			public String getName() {
				return Bukkit.getConsoleSender().getName();
			}

			@Override
			public boolean isActive() {
				return true;
			}

			@Override
			public boolean isPersistent() {
				return true;
			}

			@Override
			public UUID getUniqueId() {
				return uuid;
			}
		};
	}
}
