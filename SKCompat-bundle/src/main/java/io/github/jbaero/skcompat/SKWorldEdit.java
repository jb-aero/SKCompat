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
import com.sk89q.worldedit.math.BlockVector3;

public class SKWorldEdit {
	// reserved LocalPlayer for the Console
	private static SKConsole console;

	public static SKCommandSender GetSKPlayer(MCCommandSender sender, Target t) {
		SKCommandSender ret;
		if (sender == null || sender instanceof MCConsoleCommandSender) {
			if (console == null) {
				console = new SKConsole();
			}
			ret = console;
		} else if (sender instanceof MCPlayer) {
			ret = new SKPlayer((MCPlayer) sender);
		} else {
			throw new CRECastException("Sender type not yet supported: " + sender.getClass().getName(), t);
		}
		ret.setTarget(t);
		return ret;
	}

	public static MCCommandSender GetPlayer(Mixed arg, Target t) {
		if (arg instanceof CNull || Static.getConsoleName().equals(arg.val())) {
			return Static.getServer().getConsole();
		} else {
			return Static.GetPlayer(arg, t);
		}
	}

	public static Vector3D vtov(BlockVector3 vec) {
		return new Vector3D(vec.getX(), vec.getY(), vec.getZ());
	}
}
