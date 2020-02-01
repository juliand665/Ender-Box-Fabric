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
		fun wrapBlock(world: World, targetPos: BlockPos, newState: BlockState, isCreative: Boolean): Boolean {
			val targetState = world.getBlockState(targetPos)
			
			if (!canEnderBoxPickUp(world, targetPos, isCreative)) return false
			
			playPlacementSound(world, targetPos)
			
			if (world.isClient) return true
			
			// capture tile entity
			val targetTileEntityNBT = world.getBlockEntity(targetPos)?.toTag(CompoundTag())
			
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
			
			world.setBlockState(targetPos, newState, 3)
			
			// store captured tile entity
			EnderBoxBlockEntity.get(world, targetPos).run {
				storedBlock = BlockData(targetState, targetTileEntityNBT)
			}
			
			return true
		}
		
		fun unwrapBlock(world: World, targetPos: BlockPos, newState: (BlockData) -> BlockState): BlockData {
			val tileEntity = EnderBoxBlockEntity.get(world, targetPos)
			val blockData = tileEntity.storedBlock
			
			playPlacementSound(world, targetPos)
			
			if (world.isClient) return blockData
			
			val state = newState(blockData)
			world.setBlockState(targetPos, state, 3)
			
			blockData.updatePosition(targetPos)
			val cachedBlockEntity = blockData.cachedBlockEntity
			if (cachedBlockEntity != null) {
				world.setBlockEntity(targetPos, cachedBlockEntity)
			} else {
				blockData.blockEntityTag?.also { world.getBlockEntity(targetPos)?.fromTag(it) }
			}
			
			if (!state.canPlaceAt(world, targetPos)) {
				world.breakBlock(targetPos, true)
			}
			
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
		
		val enderBoxBlacklist: List<String> = listOf("${EnderBoxMod.modID}:ender_box") // TODO
		
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
