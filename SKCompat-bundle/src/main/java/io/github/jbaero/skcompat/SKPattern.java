package io.github.jbaero.skcompat;

import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.function.pattern.TypeApplyingPattern;
import com.sk89q.worldedit.world.block.BlockTypes;

public class SKPattern {

	Pattern pattern;
	ParserContext parserContext;

	public SKPattern(Actor user, LocalSession localSession) {
		this.parserContext = new ParserContext();
		this.parserContext.setActor(user);
		this.parserContext.setSession(localSession);
	}

	public Pattern getHandle() {
		return pattern;
	}

	public void generateBlockPattern(CString source, Target t) {
		try {
			pattern = WorldEdit.getInstance().getPatternFactory().parseFromInput(source.val(), parserContext);
		} catch(InputParseException ex) {
			throw new CREFormatException(ex.getMessage(), t);
		}
	}

	public void generateBlockPattern(CArray source, Target t) {
		if(source.isAssociative()) {
			throw new CREFormatException("Expected a normal array", t);
		}

		if (source.size() == 0) {
			pattern = new TypeApplyingPattern(((Locatable) parserContext.getActor()).getExtent(), BlockTypes.AIR.getDefaultState());
			return;
		}

		pattern = new RandomPattern();
		for (Mixed entry : source.asList()) {
			CArray src = ArgumentValidation.getArray(entry, t);
			if (!src.containsKey("block")) {
				throw new CREFormatException("Block name required", t);
			}
			double weight = 1D;
			if (src.containsKey("weight")) {
				weight = ArgumentValidation.getDouble(src.get("weight", t), t);
			}
			try {
				Pattern p = WorldEdit.getInstance().getBlockFactory().parseFromInput(src.get("block", t).val(), parserContext);
				((RandomPattern) pattern).add(p, weight);
			} catch(InputParseException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
		}
	}
}
