package enderbox

import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.CompoundTag
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.regex.Pattern

open class EnderBoxBlock(settings: Settings) : Block(settings.nonOpaque()), BlockEntityProvider {
	override fun createBlockEntity(blockView: BlockView?): BlockEntity? = EnderBoxBlockEntity()
	
	override fun isSimpleFullBlock(blockState: BlockState?, blockView: BlockView?, blockPos: BlockPos?) = false
	
	override fun allowsSpawning(blockState: BlockState?, blockView: BlockView?, blockPos: BlockPos?, entityType: EntityType<*>?) = false
	
	override fun isTranslucent(blockState: BlockState?, blockView: BlockView?, blockPos: BlockPos?): Boolean = true
	
	override fun canSuffocate(blockState: BlockState?, blockView: BlockView?, blockPos: BlockPos?): Boolean = false
	
	companion object {
		private val runningCaptures = mutableSetOf<BlockPos>()
		fun wrapBlock(world: World, targetPos: BlockPos, newState: BlockState, isCreative: Boolean): Boolean {
			if (runningCaptures.contains(targetPos)) return false // don't wrap a block while it's already getting wrapped
			val pos = targetPos.toImmutable()
			runningCaptures.add(pos)
			
			try {
				return doWrapBlock(world, targetPos, newState, isCreative)
			} finally {
				runningCaptures.remove(pos)
			}
		}
		
		private fun doWrapBlock(world: World, targetPos: BlockPos, newState: BlockState, isCreative: Boolean): Boolean {
			val targetState = world.getBlockState(targetPos)
			
			if (!canEnderBoxPickUp(world, targetPos, isCreative)) return false
			
			playPlacementSound(world, targetPos)
			
			if (world.isClient) return true
			
			// capture tile entity
			val targetBlockEntityTag = world.getBlockEntity(targetPos)?.toTag(CompoundTag())
			
			//println("wrapping block at $targetPos with state ${world.getBlockState(targetPos)}")
			
			// replace block
			// We remove the tile entity before removing the block so the breakBlock() handler can't use it to drop items or cause flux etc.
			world.removeBlockEntity(targetPos)
			// Since we removed the tile entity, we may be violating some assumptions, but that should only cause early exits from breakBlock(), which is called late enough that we don't break much.
			try {
				world.removeBlock(targetPos)
			} catch (e: Exception) {
				EnderBoxMod.logger.debug("ender boxing ignoring the following exception:")
				EnderBoxMod.logger.debug(e)
			}
			
			world.setBlockEntity(targetPos, EnderBoxBlockEntity())
			val didReplace = world.setBlockState(targetPos, newState, 0b1010) // no neighbor updates yet
			assert(didReplace)
			
			// store captured tile entity
			EnderBoxBlockEntity.get(world, targetPos).run {
				storedBlock = BlockData(targetState, targetBlockEntityTag)
			}
			
			targetState.onBlockAdded(world, targetPos, newState, false)
			world.updateNeighbors(targetPos, targetState.block)
			
			return true
		}
		
		fun unwrapBlock(world: World, targetPos: BlockPos, newState: (BlockData) -> BlockState): BlockData {
			val tileEntity = EnderBoxBlockEntity.get(world, targetPos)
			val blockData = tileEntity.storedBlock
			
			playPlacementSound(world, targetPos)
			
			if (world.isClient) return blockData
			
			val state = newState(blockData)
			world.setBlockState(targetPos, state, 0b1011)
			
			blockData.updatePosition(targetPos)
			blockData.blockEntityTag?.also { world.getBlockEntity(targetPos)?.fromTag(it) }
			
			if (!state.canPlaceAt(world, targetPos)) {
				world.breakBlock(targetPos, true)
			}
			
			// e.g. make unwrapped blocks check if they're powered
			world.updateNeighbor(targetPos, Blocks.AIR, targetPos)
			// can't call Block::onPlaced because e.g. skulls read their data from the passed ItemStack
			
			return blockData
		}
		
		fun playPlacementSound(world: World, pos: BlockPos) {
			val soundGroup = EnderBoxMod.humanEnderBoxBlock.defaultState.soundGroup
			world.playSound(
				null,
				pos,
				soundGroup.placeSound,
				SoundCategory.BLOCKS,
				(soundGroup.volume + 1.0F) / 2.0F,
				soundGroup.pitch * 0.8F
			)
		}
		
		fun canEnderBoxPickUp(blockView: BlockView, pos: BlockPos, isCreative: Boolean): Boolean {
			val blockState = blockView.getBlockState(pos)
			if (!isCreative && blockState.getHardness(blockView, pos) < 0) return false // unbreakable
			
			return !isBlacklisted(blockState.block.identifier.toString())
		}
		
		val enderBoxBlacklist: List<String> = listOf("${EnderBoxMod.modID}:ender_box", "${EnderBoxMod.modID}:ender_boxed") // TODO
		
		fun isBlacklisted(blockName: String): Boolean {
			for (glob in enderBoxBlacklist) {
				val pattern = StringBuilder(glob.length)
				for (part in glob.split("""\*""".toRegex()).toTypedArray()) {
					if (part.isNotEmpty()) {
						pattern.append(Pattern.quote(part))
					}
					pattern.append(".*")
				}
				
				// delete last ".*" wildcard
				pattern.delete(pattern.length - 2, pattern.length)
				
				if (Pattern.matches(pattern.toString(), blockName)) {
					return true
				}
			}
			return false
		}
	}
}

fun World.removeBlock(pos: BlockPos) = setBlockState(pos, Blocks.AIR.defaultState)

val Block.identifier
	get() = Registry.BLOCK.getId(this)
