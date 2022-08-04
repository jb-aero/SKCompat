package io.github.jbaero.skcompat;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;

public class SKMask {

	Mask mask;

	public SKMask() {}
	
	public Mask getHandle() {
		return mask;
	}

	public void generateMask(String source, SKCommandSender user, Target t) {
		ParserContext context = new ParserContext();
		context.setActor(user);
		context.setExtent(user.getExtent());
		try {
			mask = WorldEdit.getInstance().getMaskFactory().parseFromInput(source, context);
		} catch(InputParseException ex) {
			throw new CREFormatException(ex.getMessage(), t);
		}
	}
}
