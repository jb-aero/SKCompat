package com.zeoldcraft.skcompat;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.pattern.BlockPattern;

public class WeightedBlockPattern extends BlockPattern {

	private double weight;

	public WeightedBlockPattern(BaseBlock block, double weight) {
		super(block);
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}
}
