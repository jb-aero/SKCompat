package com.zeoldcraft.skcompat;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import com.laytonsmith.core.functions.AbstractFunction;

/**
 * @author jb_aero
 */
@MSExtension("SKCompat")
public class SKCompat extends AbstractExtension {

	@Override
	public void onStartup() {
		System.out.println("SKCompat " + getVersion() + " loaded.");
	}

	@Override
	public void onShutdown() {
		System.out.println("SKCompat " + getVersion() + " unloaded.");
	}

	@Override
	public Version getVersion() {
		return new SimpleVersion(1, 1, 7);
	}

	public static MCCommandSender myGetPlayer(Construct arg, Target t) {
		if (arg == null || Static.getConsoleName().equals(arg.val())) {
			return Static.getServer().getConsole();
		} else {
			return Static.GetPlayer(arg, t);
		}
	}

	public static abstract class SKFunction extends AbstractFunction {

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

	}
}
