package io.github.jbaero.skcompat;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.function.mask.Mask;

public class SKMask {

	Mask mask;
	ParserContext parserContext;

	public SKMask(Actor user) {
		this.parserContext = new ParserContext();
		this.parserContext.setActor(user);
		this.parserContext.setExtent(((Locatable) user).getExtent());
	}

	public Mask getHandle() {
		return mask;
	}

	public void generateMask(String source, Target t) {
		try {
			mask = WorldEdit.getInstance().getMaskFactory().parseFromInput(source, parserContext);
		} catch(InputParseException ex) {
			throw new CREFormatException(ex.getMessage(), t);
		}
	}
}
