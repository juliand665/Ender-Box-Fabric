package enderbox

import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.*
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

const val blockDataKey = "blockData"

class EnderBoxItem(settings: Settings) : BlockItem(EnderBoxMod.enderBoxBlock, settings) {
	override fun appendTooltip(itemStack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
		super.appendTooltip(itemStack, world, tooltip, context)
		
		val id = identifier
		
		tooltip.add(localized("tooltip", id, "default"))
		
		val blockData = itemStack.blockData
		val contentsDesc = if (blockData != null) {
			val block = blockData.block
			if (context.isAdvanced) {
				localized("tooltip", id, "contains_block.advanced", block.name, block.identifier)
			} else {
				localized("tooltip", id, "contains_block", block.name)
			}
		} else {
			localized("tooltip", id, "empty")
		}
		
		tooltip.add(contentsDesc.styled { it.color = Formatting.YELLOW })
	}
	
	override fun canPlace(context: ItemPlacementContext, blockState: BlockState?): Boolean {
		return if (context.stack.hasBlockData) {
			super.canPlace(context, blockState)
		} else {
			canEnderBoxPickUp(context.world, context.blockPos, context.player?.isCreative == true)
		}
	}
	
	override fun place(context: ItemPlacementContext): ActionResult {
		if (context.world.isClient) return ActionResult.SUCCESS
		
		// disallow placing empty boxes
		val blockData = context.stack.blockData
		if (blockData == null) return ActionResult.PASS
		
		val result = super.place(context)
		
		if (result == ActionResult.SUCCESS) {
			val blockEntity = EnderBoxBlockEntity.get(context.world, context.blockPos)
			blockEntity.storedBlock = blockData
		}
		
		return result
	}
	
	override fun useOnBlock(context: ItemUsageContext): ActionResult {
		if (context.stack.hasBlockData) return super.useOnBlock(context) // placement
		
		val isCreative = context.player?.isCreative == true
		val success = EnderBoxBlock.wrapBlock(
			context.world, context.blockPos,
			EnderBoxMod.enderBoxBlock.defaultState,
			isCreative
		)
		
		if (success && !isCreative) {
			context.stack.decrement(1)
		}
		
		return if (success) ActionResult.SUCCESS else ActionResult.PASS
	}
}

val ItemStack.hasBlockData
	get() = tag?.contains(blockDataKey) == true

var ItemStack.blockData: BlockData?
	get() = tag?.getCompound(blockDataKey)?.let(::BlockData)
	set(blockData) {
		blockData
			?.let { orCreateTag.put(blockDataKey, it.toTag()) }
			?: tag?.remove(blockDataKey)
	}

val Item.identifier
	get() = Registry.ITEM.getId(this)
