package io.github.jbaero.skcompat;

import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.SetLocatedBlocks;
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.util.collection.LocatedBlockList;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class WorldEditEvents {

	@Subscribe
	public void wrapEditSessionEvent(EditSessionEvent event) {
		System.out.println("ESE event wrapped.");
		event.setExtent(new EditEventExtent(event.getExtent(), event.getActor(), event.getWorld(), event.getStage()));
	}

	public class EditEventExtent extends AbstractDelegateExtent {

		private Actor actor;
		private World world;
		private EditSession.Stage stage;

		protected EditEventExtent(Extent extent, Actor actor, World world, EditSession.Stage stage) {
			super(extent);
			this.actor = actor;
			this.world = world;
			this.stage = stage;
		}
	}

	public class EditEventSingleExtent extends EditEventExtent {

		EditEventSingleExtent(Extent extent, Actor actor, World world, EditSession.Stage stage) {
			super(extent, actor, world, stage);
		}

		@Override
		public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
			WorldEditEventSingle worldEditEvent = new WorldEditEventSingle(this, block, location);
			EventUtils.TriggerListener(Driver.EXTENSION, "world_edit_single", worldEditEvent);
			if (worldEditEvent.cancelled) {
				return false;
			}
			return super.setBlock(worldEditEvent.getLocation(), worldEditEvent.getBlock());
		}
	}

	public class EditEventMultiExtent extends EditEventExtent {

		protected EditEventMultiExtent(Extent extent, Actor actor, World world, EditSession.Stage stage) {
			super(extent, actor, world, stage);
		}

		private LocatedBlockList editList = new LocatedBlockList();

		@Override
		public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
			System.out.println("setBlock was called");
			editList.add(new LocatedBlock(location, block));
			return !getBlock(location).equalsFuzzy(block);
		}

		@Override
		protected Operation commitBefore() {
			System.out.println("commmitBefore was called");
			WorldEditEventMulti worldEditEvent = new WorldEditEventMulti(this);
			EventUtils.TriggerListener(Driver.EXTENSION, "world_edit_multi", worldEditEvent);
			if (!worldEditEvent.cancelled) {
				System.out.println("event not cancelled!");
				return new SetLocatedBlocks(getExtent(), editList);
			}
			return super.commitBefore();
		}
	}

	public class WorldEditEvent implements BindableEvent {

		boolean cancelled = false;
		EditEventExtent extent;

		WorldEditEvent(EditEventExtent extent) {
			this.extent = extent;
		}

		Actor getActor() {
			return extent.actor;
		}

		World getWorld() {
			return extent.world;
		}

		EditSession.Stage getStage() {
			return extent.stage;
		}

		@Override
		public Object _GetObject() {
			return null;
		}
	}

	public class WorldEditEventSingle extends WorldEditEvent {

		BlockStateHolder block;
		Vector vector;
		WorldEditEventSingle(EditEventExtent extent, BlockStateHolder block, Vector vector) {
			super(extent);
		}

		public Vector getLocation() {
			return vector;
		}

		public void setLocation(Vector vector) {
			this.vector = vector;
		}

		public BlockStateHolder getBlock() {
			return block;
		}

		public void setBlock(BlockStateHolder holder) {
			this.block = holder;
		}
	}

	public class WorldEditEventMulti extends WorldEditEvent {

		EditEventMultiExtent extent;
		WorldEditEventMulti(EditEventMultiExtent extent) {
			super(extent);
			this.extent = extent;
		}

		public LocatedBlockList getEditList() {
			return extent.editList;
		}

		public void setEditList(LocatedBlockList editList) {
			extent.editList = editList;
		}
	}
}
