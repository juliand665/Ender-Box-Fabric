package enderbox

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.IWorld
import net.minecraft.world.World
import java.util.*

class EnderBoxerBlock(settings: Settings) : Block(settings.nonOpaque()) {
	init {
		defaultState = stateManager.defaultState
			.with(facing, Direction.SOUTH)
	}
	
	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		builder.add(facing)
	}
	
	override fun getPlacementState(context: ItemPlacementContext): BlockState? = defaultState
		.with(facing, context.playerLookDirection.opposite)
	
	override fun rotate(blockState: BlockState, rotation: BlockRotation): BlockState = blockState
		.with(facing, rotation.rotate(blockState.get(facing)))
	
	override fun mirror(blockState: BlockState, mirror: BlockMirror): BlockState = blockState
		.with(facing, mirror.apply(blockState.get(facing)))
	
	override fun onPlaced(world: World, pos: BlockPos, blockState: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
		super.onPlaced(world, pos, blockState, placer, itemStack)
		
		updateState(world, pos, blockState)
	}
	
	override fun onBroken(world: IWorld, pos: BlockPos, blockState: BlockState) {
		super.onBroken(world, pos, blockState)
		
		(world as? World)?.let {
			releaseBlock(it, pos, blockState)
		}
	}
	
	override fun scheduledTick(blockState: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
		@Suppress("deprecation")
		super.scheduledTick(blockState, world, pos, random)
		
		updateState(world, pos, blockState)
	}
	
	override fun neighborUpdate(blockState: BlockState, world: World, pos: BlockPos, block: Block, otherPos: BlockPos, flag: Boolean) {
		@Suppress("deprecation")
		super.neighborUpdate(blockState, world, pos, block, otherPos, flag)
		
		updateState(world, pos, blockState)
	}
	
	private fun updateState(world: World, pos: BlockPos, blockState: BlockState) {
		val hasBlock = isTargetBoxed(world, pos, blockState)
		val isPowered: Boolean = world.isReceivingRedstonePower(pos)
		if (hasBlock && !isPowered) {
			releaseBlock(world, pos, blockState)
		} else if (!hasBlock && isPowered) {
			captureBlock(world, pos, blockState)
		}
	}
	
	private fun isTargetBoxed(world: IWorld, pos: BlockPos, blockState: BlockState): Boolean {
		val targetPos = pos.offset(blockState.get(facing))
		val targetState = world.getBlockState(targetPos)
		return targetState.block is MachineEnderBoxBlock
	}
	
	private fun captureBlock(world: World, pos: BlockPos, blockState: BlockState) {
		if (isTargetBoxed(world, pos, blockState)) return  // already boxed
		
		val targetPos = pos.offset(blockState.get(facing))
		EnderBoxBlock.wrapBlock(
			world, targetPos,
			EnderBoxMod.machineEnderBoxBlock.defaultState,
			isCreative = false
		)
	}
	
	private fun releaseBlock(world: World, pos: BlockPos, blockState: BlockState) {
		if (!isTargetBoxed(world, pos, blockState)) return // not boxed
		
		val targetPos = pos.offset(blockState.get(facing))
		EnderBoxBlock.unwrapBlock(world, targetPos, BlockData::blockState)
	}
	
	companion object {
		val facing: DirectionProperty = Properties.FACING
	}
}
