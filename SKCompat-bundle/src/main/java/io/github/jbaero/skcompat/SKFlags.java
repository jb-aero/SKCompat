package io.github.jbaero.skcompat;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.*;

import java.util.*;

public class SKFlags {

	static final Map<String, Flag<?>> FLAG_CACHE = new HashMap<>();

	public static Flag<?> GetFlag(String flagName, Target t) {
		// caching here because we support dashes as optional, which would require looping through all flags every time
		Flag<?> cachedFlag = FLAG_CACHE.get(flagName);
		if(cachedFlag == null) {
			String filteredFlag = flagName.replace("-", "");
			for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry().getAll()) {
				if (flag.getName().replace("-", "").equalsIgnoreCase(filteredFlag)) {
					FLAG_CACHE.put(flagName, flag);
					return flag;
				}
			}
			throw new CREPluginInternalException(String.format("Unknown flag specified: (%s).", flagName), t);
		}
		return cachedFlag;
	}

	public static Mixed ConvertFlagValue(Flag<?> flag, Object flagValue, Target t) {
		if (flag instanceof StateFlag) {
			StateFlag.State state = ((StateFlag) flag).unmarshal(flagValue);
			if (state != null) {
				return CBoolean.get(state == StateFlag.State.ALLOW);
			} else {
				return CNull.NULL;
			}
		} else if (flag instanceof BooleanFlag) {
			Boolean value = ((BooleanFlag) flag).unmarshal(flagValue);
			if (value != null) {
				return CBoolean.get(value);
			} else {
				return CNull.NULL;
			}
		} else if (flag instanceof DoubleFlag) {
			Double value = ((DoubleFlag) flag).unmarshal(flagValue);
			if (value != null) {
				return new CDouble(value, t);
			} else {
				return CNull.NULL;
			}
		} else if (flag instanceof EnumFlag) {
			return new CString(((EnumFlag<?>) flag).unmarshal(flag).name(), t);
		} else if (flag instanceof IntegerFlag) {
			Integer value = ((IntegerFlag) flag).unmarshal(flagValue);
			if (value != null) {
				return new CInt(value, t);
			} else {
				return CNull.NULL;
			}
		} else if (flag instanceof LocationFlag) {
			Location loc = null;
			if(flagValue instanceof Map) {
				loc = ((LocationFlag) flag).unmarshal(flagValue);
			} else if(flagValue instanceof Location) {
				loc = (Location) flagValue;
			}
			if(loc == null) {
				return CNull.NULL;
			}
			return ObjectGenerator.GetGenerator().location(new BukkitMCLocation(BukkitAdapter.adapt(loc)), true);
		} else if (flag instanceof SetFlag) {
			CArray values = new CArray(t);
			Set<?> setValue = ((SetFlag<?>) flag).unmarshal(flagValue);
			if (setValue != null) {
				for (Object setFlag : setValue) {
					if (setFlag instanceof String) {
						String value = (String) setFlag;
						values.push(new CString(value, t), t);
					} else if (setFlag instanceof EntityType) {
						String value = ((EntityType) setFlag).getName();
						values.push(new CString(value, t), t);
					} else {
						ConfigRuntimeException.DoWarning("One of the element of flag has unknown type."
								+ " This is a developer mistake, please file a ticket.");
					}
				}
			}
			return values;

		} else if (flag instanceof StringFlag) {
			String value = ((StringFlag) flag).unmarshal(flagValue);
			if (value != null) {
				return new CString(value, t);
			} else {
				return CNull.NULL;
			}
		} else if (flag instanceof RegistryFlag) {
			Keyed value = ((RegistryFlag<? extends Keyed>) flag).unmarshal(flagValue);
			if (value != null) {
				return new CString(value.toString(), t);
			} else {
				return CNull.NULL;
			}
		}
		throw new CRENotFoundException("The flag type is unknown. This is a developer mistake, please file a ticket.", t);
	}

	private enum FlagType {
		BOOLEAN(BooleanFlag.class),
		DOUBLE(DoubleFlag.class),
		INTEGER(IntegerFlag.class),
		LOCATION(LocationFlag.class),
		STRING(StringFlag.class);

		final Class<?> flagClass;

		FlagType(Class<?> flagClass) {
			this.flagClass = flagClass;
		}
	}

	public static Flag<?> CreateFlag(String flagName, String flagType, Target t) {
		if(!Flag.isValidName(flagName)) {
			throw new CREFormatException("Invalid flag name.", t);
		}
		try {
			Class<?> flagClass = FlagType.valueOf(flagType.toUpperCase()).flagClass;
			return (Flag<?>) ReflectionUtils.newInstance(flagClass, new Class[]{String.class}, new Object[]{flagName});
		} catch (IllegalArgumentException ex) {
			throw new CREIllegalArgumentException("Invalid flag type: " + flagType + ". Must be one of: "
					+ StringUtils.Join(FlagType.values(), ", ", ", or "), t);
		} catch (ReflectionUtils.ReflectionException ex) {
			throw new CREException("Failed to create new flag.", t);
		}
	}
}
