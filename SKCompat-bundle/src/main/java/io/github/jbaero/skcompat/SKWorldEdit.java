package io.github.jbaero.skcompat;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import org.bukkit.entity.Player;

public class SKWorldEdit {
	private static SKConsole console;

	public static Actor GetActor(MCCommandSender sender, Target t) {
		if (sender == null || sender instanceof MCConsoleCommandSender) {
			if (console == null) {
				console = new SKConsole();
			}
			return console;
		} else if (sender instanceof MCPlayer) {
			return new SKPlayer((Player) sender.getHandle());
		}
		throw new CRECastException("Sender type not yet supported: " + sender.getClass().getName(), t);
	}

	public static MCCommandSender GetSender(Mixed arg, Target t) {
		if (arg instanceof CNull || Static.getConsoleName().equals(arg.val())) {
			return Static.getServer().getConsole();
		} else {
			return Static.GetPlayer(arg, t);
		}
	}

	public static LocalSession GetLocalSession(Actor actor) {
		return WorldEdit.getInstance().getSessionManager().get(actor);
	}

	public static EditSession GetEditSession(Actor actor, boolean fastMode) {
		GetLocalSession(actor).setSideEffectSet(fastMode ? SideEffectSet.none() : SideEffectSet.defaults());
		Extent extent = ((Locatable) actor).getExtent();
		World world = null;
		if(extent instanceof World) {
			world = (World) extent;
		}
		return WorldEdit.getInstance().newEditSessionBuilder()
				.world(world)
				.maxBlocks(-1)
				.blockBag(null)
				.actor(actor)
				.build();
	}

	public static Vector3D vtov(BlockVector3 vec) {
		return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
	}
}
