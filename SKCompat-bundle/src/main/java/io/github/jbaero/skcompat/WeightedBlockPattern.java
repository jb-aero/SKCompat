package io.github.jbaero.skcompat;

import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class WeightedBlockPattern extends BlockPattern {

	private double weight;

	public WeightedBlockPattern(BlockStateHolder block, double weight) {
		super(block);
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}
}
