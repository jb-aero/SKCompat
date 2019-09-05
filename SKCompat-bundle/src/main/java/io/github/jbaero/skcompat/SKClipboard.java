package io.github.jbaero.skcompat;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.*;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import org.bukkit.Location;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SKClipboard {

	public static void Copy(MCCommandSender sender, MCLocation loc, boolean entities, boolean biomes, Target t) {
		SKCommandSender user = SKWorldEdit.GetSKPlayer(sender, t);
		LocalSession session = user.getLocalSession();
		EditSession editSession = user.getEditSession(false);
		try {
			Region region = session.getSelection(user.getWorld());
			BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
			BlockVector3 pos = session.getPlacementPosition(user);
			if(loc != null) {
				pos = BukkitAdapter.asBlockVector((Location) loc.getHandle());
			}
			clipboard.setOrigin(pos);
			ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
			copy.setCopyingEntities(entities);
			copy.setCopyingBiomes(biomes);
			Operations.complete(copy);
			user.getLocalSession().setClipboard(new ClipboardHolder(clipboard));
		} catch (Exception wee) {
			throw new CREPluginInternalException(wee.getMessage(), t);
		}
	}

	public static void Load(MCCommandSender sender, String filename, Target t) {
		WorldEdit worldEdit = WorldEdit.getInstance();
		SKCommandSender user = SKWorldEdit.GetSKPlayer(sender, t);

		File dir = worldEdit.getWorkingDirectoryFile(worldEdit.getConfiguration().saveDir);
		File f;

		try {
			f = worldEdit.getSafeOpenFile(user, dir, filename, "schem", "schematic");
		} catch (Exception fne) {
			throw new CREFormatException(fne.getMessage(), t);
		}

		try (Closer closer = Closer.create()) {
			FileInputStream fis = closer.register(new FileInputStream(f));
			BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
			ClipboardReader reader;
			if (f.getName().endsWith(".schem")) {
				reader = closer.register(BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(bis));
			} else {
				// legacy schematic format
				reader = closer.register(BuiltInClipboardFormat.MCEDIT_SCHEMATIC.getReader(bis));
			}

			Clipboard clipboard = reader.read();
			user.getLocalSession().setClipboard(new ClipboardHolder(clipboard));
		} catch (IOException e) {
			throw new CREIOException("Schematic could not read or it does not exist: " + e.getMessage(), t);
		}
	}
	
	public static void Save(MCCommandSender sender, String filename, Target t) {
		WorldEdit worldEdit = WorldEdit.getInstance();
		SKCommandSender user = SKWorldEdit.GetSKPlayer(sender, t);

		Clipboard clipboard;
		try {
			clipboard = user.getLocalSession().getClipboard().getClipboard();
		} catch (Exception e) {
			throw new CRENotFoundException("The clipboard is empty, copy something to it first!", t);
		}

		File dir = worldEdit.getWorkingDirectoryFile(worldEdit.getConfiguration().saveDir);

		File f;

		try {
			f = worldEdit.getSafeSaveFile(user, dir, filename, "schem");
		} catch (Exception fne) {
			throw new CREFormatException(fne.getMessage(), t);
		}

		try(Closer closer = Closer.create()) {
			f.createNewFile();
			FileOutputStream fos = closer.register(new FileOutputStream(f));
			BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
			ClipboardWriter writer = closer.register(BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(bos));
			writer.write(clipboard);
		} catch (IOException e) {
			throw new CREIOException("Schematic could not read or it does not exist: " + e.getMessage(), t);
		}
	}
	
	public static void Rotate(MCCommandSender sender, int xaxis, int yaxis, int zaxis, Target t) {
		SKCommandSender user = SKWorldEdit.GetSKPlayer(sender, t);
		LocalSession session = user.getLocalSession();

		ClipboardHolder holder;
		try {
			holder = session.getClipboard();
		} catch (Exception e) {
			throw new CRENotFoundException("The clipboard is empty, copy something to it first!", t);
		}

		AffineTransform transform = new AffineTransform();
		transform = transform.rotateY(-yaxis);
		transform = transform.rotateX(-xaxis);
		transform = transform.rotateZ(-zaxis);
		holder.setTransform(holder.getTransform().combine(transform));
	}
	
	public static void Paste(MCCommandSender sender, boolean airless, boolean biomes, boolean entities, boolean fastMode, 
				boolean origin, boolean select, Target t) {

		SKCommandSender user = SKWorldEdit.GetSKPlayer(sender, t);

		// from com.sk89q.worldedit.command.ClipboardCommands.paste()
		EditSession editSession = user.getEditSession(fastMode);
		LocalSession session = user.getLocalSession();

		ClipboardHolder holder;
		try {
			holder = session.getClipboard();
		} catch (Exception e) {
			throw new CRENotFoundException("The clipboard is empty, copy something to it first!", t);
		}

		Clipboard clipboard = holder.getClipboard();
		Region region = clipboard.getRegion();

		BlockVector3 to;
		try {
			to = origin ? clipboard.getOrigin() : session.getPlacementPosition(user);
		} catch (Exception e) {
			throw new CRENotFoundException("The clipboard is empty, copy something to it first!", t);
		}

		Operation operation = holder
				.createPaste(editSession)
				.to(to)
				.ignoreAirBlocks(airless)
				.copyEntities(entities)
				.copyBiomes(biomes)
				.build();

		try {
			Operations.completeLegacy(operation);
			if (select) {
				BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
				Vector3 realTo = to.toVector3().add(holder.getTransform().apply(clipboardOffset.toVector3()));
				Vector3 max = realTo.add(holder.getTransform().apply(region.getMaximumPoint().subtract(region.getMinimumPoint()).toVector3()));
				RegionSelector selector = new CuboidRegionSelector(user.getWorld(), realTo.toBlockPoint(), max.toBlockPoint());
				session.setRegionSelector(user.getWorld(), selector);
				selector.learnChanges();
				selector.explainRegionAdjust(user, session);
			}
		} catch (Exception e) {
			throw new CRERangeException("Attempted to change more blocks than allowed.", t);
		} finally {
			editSession.flushSession();
		}

	}
}
