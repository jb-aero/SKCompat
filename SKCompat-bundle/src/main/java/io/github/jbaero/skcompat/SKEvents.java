package io.github.jbaero.skcompat;

import com.laytonsmith.abstraction.events.MCPlayerCommandEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.listener.WorldGuardPlayerListener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class SKEvents {

	// @event
	public void onCommand(MCPlayerCommandEvent event) {
		WorldGuardPlayerListener wgpl = new WorldGuardPlayerListener(WorldGuardPlugin.inst());
		wgpl.onPlayerCommandPreprocess((PlayerCommandPreprocessEvent) event._GetObject());
	}
}
