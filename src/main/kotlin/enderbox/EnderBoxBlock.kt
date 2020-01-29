package enderbox

import net.fabricmc.fabric.api.block.FabricMaterialBuilder
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.regex.Pattern

val enderBoxMaterial: Material = FabricMaterialBuilder(MaterialColor.BLACK)
	.lightPassesThrough()
	.notSolid()
	.build()

class EnderBoxBlock(settings: Settings) : Block(settings.nonOpaque()), BlockEntityProvider {
	companion object {
		fun wrapBlock(world: World, targetPos: BlockPos, newState: BlockState): Boolean {
			val targetState = world.getBlockState(targetPos)
			
			if (!canEnderBoxPickUp(world, targetPos)) return false
			
			// TODO SoundHelpers.playPlacementSound(world, targetPos, this)
			
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
			
			//SoundHelpers.playPlacementSound(world, targetPos, this)
			
			if (world.isClient) return blockData
			
			world.setBlockState(targetPos, newState(blockData), 3)
			
			blockData.updatePosition(targetPos)
			blockData.blockEntityTag?.also { world.getBlockEntity(targetPos)?.fromTag(it) }
			
			world.blockTickScheduler.schedule(targetPos, blockData.block, 0) // e.g. makes sugar cane pop off if placed invalidly, but unfortunately doesn't affect cactus
			
			return blockData
		}
	}
	
	override fun createBlockEntity(blockView: BlockView?): BlockEntity? = EnderBoxBlockEntity()
	
	override fun isSimpleFullBlock(blockState: BlockState?, blockView: BlockView?, blockPos: BlockPos?) = false
	
	override fun allowsSpawning(blockState: BlockState?, blockView: BlockView?, blockPos: BlockPos?, entityType: EntityType<*>?) = false
	
	override fun isTranslucent(blockState: BlockState?, blockView: BlockView?, blockPos: BlockPos?): Boolean = true
	
	override fun canSuffocate(blockState: BlockState?, blockView: BlockView?, blockPos: BlockPos?): Boolean = false
	
	override fun onUse(blockState: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hitResult: BlockHitResult): ActionResult {
		val newState = { blockData: BlockData ->
			val context = ItemUsageContext(player, hand, hitResult)
			blockData.block.getPlacementState(ItemPlacementContext(context))!!
		}
		
		val placed = unwrapBlock(world, pos, newState)
		
		if (world.isClient) return ActionResult.SUCCESS
		
		placed.block.onPlaced(world, pos, newState(placed), player, placed.pickedBlock(world, pos))
		
		if (!player.isCreative) {
			ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), ItemStack(EnderBoxMod.enderBoxBlock))
		}
		
		return ActionResult.SUCCESS
	}
}

fun canEnderBoxPickUp(blockView: BlockView, pos: BlockPos): Boolean {
	val blockState = blockView.getBlockState(pos)
	if (blockState.getHardness(blockView, pos) < 0) return false // unbreakable
	
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

fun World.removeBlock(pos: BlockPos) = setBlockState(pos, Blocks.AIR.defaultState)

val Block.identifier
	get() = Registry.BLOCK.getId(this)
