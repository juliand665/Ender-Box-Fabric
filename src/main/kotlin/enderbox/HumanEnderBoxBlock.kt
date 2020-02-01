package enderbox

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class HumanEnderBoxBlock(settings: Settings) : EnderBoxBlock(settings) {
	override fun onUse(blockState: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hitResult: BlockHitResult): ActionResult {
		val newState = { blockData: BlockData ->
			if (player.isSneaking) {
				blockData.blockState
			} else {
				val context = ItemUsageContext(player, hand, hitResult)
				blockData.block.getPlacementState(ItemPlacementContext(context)) ?: blockData.blockState
			}
		}
		
		val placed = unwrapBlock(world, pos, newState)
		
		if (world.isClient) return ActionResult.SUCCESS
		
		placed.block.onPlaced(world, pos, newState(placed), player, placed.pickedBlock(world, pos))
		
		if (!player.isCreative) {
			ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), ItemStack(EnderBoxMod.humanEnderBoxBlock))
		}
		
		return ActionResult.SUCCESS
	}
	
	override fun getPickStack(blockView: BlockView, pos: BlockPos, blockState: BlockState): ItemStack {
		return ItemStack(this).apply {
			blockData = EnderBoxBlockEntity.get(blockView, pos).storedBlock
		}
	}
}