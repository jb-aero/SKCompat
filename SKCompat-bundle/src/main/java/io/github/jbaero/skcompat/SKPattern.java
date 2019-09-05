package io.github.jbaero.skcompat;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.function.pattern.TypeApplyingPattern;
import com.sk89q.worldedit.world.block.BlockTypes;

public class SKPattern {

	Pattern pattern;

	public SKPattern() {}
	
	public Pattern getHandle() {
		return pattern;
	}

	public void generateBlockPattern(CString source, SKCommandSender user, Target t) {
		ParserContext context = new ParserContext();
		context.setActor(user);
		try {
			pattern = WorldEdit.getInstance().getPatternFactory().parseFromInput(source.val(), context);
		} catch(InputParseException ex) {
			throw new CREFormatException(ex.getMessage(), t);
		}
	}

	public void generateBlockPattern(CArray source, SKCommandSender user, Target t) {
		if(source.isAssociative()) {
			throw new CREFormatException("Expected a normal array", t);
		}

		if (source.size() == 0) {
			pattern = new TypeApplyingPattern(user.getExtent(), BlockTypes.AIR.getDefaultState());
			return;
		}

		pattern = new RandomPattern();
		for (Mixed entry : source.asList()) {
			ParserContext context = new ParserContext();
			context.setActor(user);
			CArray src = Static.getArray(entry, t);
			if (!src.containsKey("block")) {
				throw new CREFormatException("Block name required", t);
			}
			double weight = 1D;
			if (src.containsKey("weight")) {
				weight = Static.getDouble(src.get("weight", t), t);
			}
			try {
				Pattern p = WorldEdit.getInstance().getBlockFactory().parseFromInput(src.get("block", t).val(), context);
				((RandomPattern) pattern).add(p, weight);
			} catch(InputParseException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
		}
	}
}
