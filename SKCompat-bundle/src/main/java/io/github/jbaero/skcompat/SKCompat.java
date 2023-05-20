package io.github.jbaero.skcompat;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import com.laytonsmith.core.functions.AbstractFunction;

/**
 * @author jb_aero
 */
@MSExtension(PomData.NAME)
public class SKCompat extends AbstractExtension {

	@Override
	public void onStartup() {
		Static.getLogger().info(PomData.NAME + " " + PomData.VERSION + " loaded.");
	}

	@Override
	public void onShutdown() {
		Static.getLogger().info(PomData.NAME + " " + PomData.VERSION + " unloaded.");
	}

	@Override
	public Version getVersion() {
		return new SimpleVersion(PomData.VERSION);
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
