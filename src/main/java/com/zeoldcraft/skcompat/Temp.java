package com.zeoldcraft.skcompat;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCOfflinePlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Temp {

	public static MCPlayer getPlayer(String name) {
		Player p = Bukkit.getServer().getPlayer(name);
		if (p == null) {
			return null;
		}
		return new BukkitMCPlayer(p);
	}

	public static MCPlayer getPlayer(UUID uuid) {
		Player p = Bukkit.getServer().getPlayer(uuid);
		if (p == null) {
			return null;
		}
		return new BukkitMCPlayer(p);
	}

	public static MCOfflinePlayer getOfflinePlayer(String player) {
		return ReflectionUtils.newInstance(
				BukkitMCOfflinePlayer.class,
				new Class[]{MCOfflinePlayer.class},
				new Object[]{Bukkit.getServer().getOfflinePlayer(player)}
		);
	}

	public static MCOfflinePlayer getOfflinePlayer(UUID uuid) {
		return ReflectionUtils.newInstance(
				BukkitMCOfflinePlayer.class,
				new Class[]{MCOfflinePlayer.class},
				new Object[]{Bukkit.getServer().getOfflinePlayer(uuid)}
		);
	}

	private static final Pattern DASHLESS_PATTERN = Pattern.compile("^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");
	/**
	 * Based on https://github.com/sk89q/SquirrelID
	 *
	 * @param subject
	 * @param t
	 * @return
	 */
	public static UUID GetUUID(String subject, Target t) {
		try {
			if (subject.length() == 36) {
				return UUID.fromString(subject);
			}
			if (subject.length() == 32) {
				Matcher matcher = DASHLESS_PATTERN.matcher(subject);
				if (!matcher.matches()) {
					throw new IllegalArgumentException("Invalid UUID format.");
				}
				return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
			} else {
				throw new ConfigRuntimeException("A UUID is expected to be 32 or 36 characters,"
						+ " but the given string was " + subject.length() + " characters.",
						ExceptionType.LengthException, t);
			}
		} catch (IllegalArgumentException iae) {
			throw new ConfigRuntimeException("A UUID length string was given, but was not a valid UUID.",
					ExceptionType.IllegalArgumentException, t);
		}
	}
	public static MCOfflinePlayer GetUser(Construct search, Target t) {
		return GetUser(search.val(), t);
	}
	/**
	 * Provides a user object containing info that doesn't require an online player.
	 * If provided a string between 1 and 16 characters, the lookup will be name-based.
	 * If provided a string that is 32 or 36 characters, the lookup will be uuid-based.
	 *
	 * @param search The text to be searched, can be between 1 and 16 characters, or 32 or 36 characters
	 * @param t
	 * @return
	 */
	public static MCOfflinePlayer GetUser(String search, Target t) {
		MCOfflinePlayer ofp;
		if (search.length() > 0 && search.length() <= 16) {
			ofp = getOfflinePlayer(search);
		} else {
			try {
				ofp = getOfflinePlayer(GetUUID(search, t));
			} catch (ConfigRuntimeException cre) {
				if (cre.getExceptionType().equals(ExceptionType.LengthException)) {
					throw new ConfigRuntimeException("The given string was the wrong size to identify a player."
							+ " A player name is expected to be between 1 and 16 characters. " + cre.getMessage(),
							ExceptionType.LengthException, t);
				} else {
					throw cre;
				}
			}
		}
		return ofp;
	}
	/**
	 * Returns the player specified by name. Injected players also are returned in this list.
	 * If provided a string between 1 and 16 characters, the lookup will be name-based.
	 * If provided a string that is 32 or 36 characters, the lookup will be uuid-based.
	 *
	 * @param player
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public static MCPlayer GetPlayer(String player, Target t) throws ConfigRuntimeException {
		MCCommandSender m;
		if (player.length() > 0 && player.length() <= 16) {
			m = GetCommandSender(player, t);
		} else {
			try {
				m = getPlayer(GetUUID(player, t));
			} catch (ConfigRuntimeException cre) {
				if (cre.getExceptionType().equals(ExceptionType.LengthException)) {
					throw new ConfigRuntimeException("The given string was the wrong size to identify a player."
							+ " A player name is expected to be between 1 and 16 characters. " + cre.getMessage(),
							ExceptionType.LengthException, t);
				} else {
					throw cre;
				}
			}
		}
		if (m == null) {
			throw new ConfigRuntimeException("The specified player (" + player + ") is not online",
					ExceptionType.PlayerOfflineException, t);
		}
		if (!(m instanceof MCPlayer)) {
			throw new ConfigRuntimeException("Expecting a player name, but \"" + player + "\" was found.",
					ExceptionType.PlayerOfflineException, t);
		}
		MCPlayer p = (MCPlayer) m;
		if (!p.isOnline()) {
			throw new ConfigRuntimeException("The specified player (" + player + ") is not online",
					ExceptionType.PlayerOfflineException, t);
		}
		return p;
	}
	/**
	 * Returns the specified command sender. Players are supported, as is the
	 * special ~console user. The special ~console user will always return a
	 * user.
	 *
	 * @param player
	 * @param t
	 * @return
	 * @throws ConfigRuntimeException
	 */
	public static MCCommandSender GetCommandSender(String player, Target t) throws ConfigRuntimeException {
		MCCommandSender m = null;
		Map<String, MCCommandSender> injectedPlayers = (Map<String, MCCommandSender>) ReflectionUtils.get(Static.class, "injectedPlayers");
		if (injectedPlayers.containsKey(player)) {
			m = injectedPlayers.get(player);
		} else {
			if (Static.getConsoleName().equals(player)) {
				m = Static.getServer().getConsole();
			} else {
				try {
					m = Static.getServer().getPlayer(player);
				} catch (Exception e) {
//Apparently the server can occasionally throw exceptions here, so instead of rethrowing
//a NPE or whatever, we'll assume that the player just isn't online, and
//throw a CRE instead.
				}
			}
		}
		if (m == null || (m instanceof MCPlayer && (!((MCPlayer) m).isOnline() && !injectedPlayers.containsKey(player)))) {
			throw new ConfigRuntimeException("The specified player (" + player + ") is not online", ExceptionType.PlayerOfflineException, t);
		}
		return m;
	}
	public static MCPlayer GetPlayer(Construct player, Target t) throws ConfigRuntimeException {
		return GetPlayer(player.val(), t);
	}
	/**
	 * If the sender is a player, it is returned, otherwise a
	 * ConfigRuntimeException is thrown.
	 *
	 * @param environment
	 * @param t
	 * @return
	 */
	public static MCPlayer getPlayer(Environment environment, Target t) {
		MCPlayer player = environment.getEnv(CommandHelperEnvironment.class).GetPlayer();
		if (player != null) {
			return player;
		} else {
			throw new ConfigRuntimeException("The passed arguments induce that the function must be run by a player.", ExceptionType.PlayerOfflineException, t);
		}
	}
}
