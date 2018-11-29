package io.github.jbaero.skcompat;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 * @author jb_aero
 */
@MSExtension(PomData.NAME)
public class SKCompat extends AbstractExtension {

	@Override
	public void onStartup() {
		System.out.println(PomData.NAME + " " + PomData.VERSION + " loaded.");
	}

	@Override
	public void onShutdown() {
		System.out.println(PomData.NAME + " " + PomData.VERSION + " unloaded.");
	}

	@Override
	public Version getVersion() {
		return new SimpleVersion(PomData.VERSION);
	}

	public static MCCommandSender myGetPlayer(Mixed arg, Target t) {
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
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return false;
		}

	}
}
