package com.zeoldcraft.skcompat;

import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import java.util.UUID;

/**
 *
 * @author jb_aero
 */
public class ConsoleSKPlayer extends AbstractPlayerActor implements SessionKey {

	private static final UUID uuid = UUID.fromString("43337e14-6cdc-45fc-b136-efd95f17a366");
	private final MCConsoleCommandSender console;
	private MCLocation location;
	private MCWorld world;
	private Target t;
	
	public ConsoleSKPlayer() {
		console = StaticLayer.GetServer().getConsole();
	}
	
	public void setTarget(Target target) {
		t = target;
	}

	@Override
	public Location getLocation() {
		return new Location(getWorld(), location.getX(), location.getY(),
				location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public void setLocation(MCLocation loc) {
		location = loc;
		world = loc.getWorld();
	}
	
	public void setWorld(MCWorld w) {
		world = w;
		location = StaticLayer.GetLocation(w, location.getX(), location.getY(),
				location.getZ(), location.getYaw(), location.getPitch());
	}

	@Override
	public World getWorld() {
		for ( World w : WorldEdit.getInstance().getServer().getWorlds() ) {
			if (w.getName().equals(world.getName())) {
				return w;
			}
		}
		return WorldEdit.getInstance().getServer().getWorlds().get(0);
	}

	@Override
	public String getName() {
		return console.getName();
	}

	@Override
	public void printRaw(String string) {
		console.sendMessage(string);
	}

	@Override
	public void printDebug(String string) {
		CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.DEBUG, string, t);
	}

	@Override
	public void print(String string) {
		CHLog.GetLogger().Log(CHLog.Tags.RUNTIME, LogLevel.DEBUG, string, t);
	}

	@Override
	public void printError(String string) {
		throw new ConfigRuntimeException(string, Exceptions.ExceptionType.PluginInternalException, t);
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
	public boolean hasPermission(String string) {
		// TODO: console can have permissions set
		return true;
	}

	@Override
	public int getItemInHand() {
		return 0;
	}

	@Override
	public void giveItem(int i, int i1) {}

	@Override
	public WorldVector getPosition() {
		return new WorldVector(getLocation());
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

	@Override
	public BaseEntity getState() {
		throw new UnsupportedOperationException("Unstable object.");
	}

	@Override
	public <T> T getFacet(Class<? extends T> type) {
		return null;
	}

	@Override
	public SessionKey getSessionKey() {
		return this;
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
	public BlockBag getInventoryBlockBag() {
		return new ConsoleBlockBag();
	}
	
	private static class ConsoleBlockBag extends BlockBag {

		@Override
		public void flushChanges() {}

		@Override
		public void addSourcePosition(WorldVector wv) {}

		@Override
		public void addSingleSourcePosition(WorldVector wv) {}

		@Override
		public void storeItem(BaseItem item) throws BlockBagException {}

		@Override
		public void fetchItem(BaseItem item) throws BlockBagException {}
		
	}

}
