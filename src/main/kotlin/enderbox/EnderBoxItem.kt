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
	override fun appendTooltip(itemStack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
		super.appendTooltip(itemStack!!, world, tooltip!!, context!!)
		
		tooltip.add(localized("tooltip", identifier, "default"))
		
		val contentsDesc = if (itemStack.hasBlockData) {
			val block = itemStack.blockData.block
			if (context.isAdvanced) {
				localized("tooltip", identifier, "contains_block.advanced", block.name, block.identifier)
			} else {
				localized("tooltip", identifier, "contains_block", block.name)
			}
		} else {
			localized("tooltip", identifier, "empty")
		}
		
		tooltip.add(contentsDesc.styled { it.color = Formatting.YELLOW })
	}
	
	override fun canPlace(context: ItemPlacementContext?, blockState: BlockState?): Boolean {
		return if (context!!.stack.hasBlockData) {
			super.canPlace(context, blockState)
		} else {
			canEnderBoxPickUp(context.world, context.blockPos)
		}
	}
	
	override fun place(context: ItemPlacementContext?): ActionResult {
		if (context!!.world.isClient) return ActionResult.SUCCESS
		
		// disallow placing empty boxes
		if (!context.stack.hasBlockData) return ActionResult.PASS
		
		val result = super.place(context)
		
		if (result == ActionResult.SUCCESS) {
			val blockEntity = EnderBoxBlockEntity.get(context.world, context.blockPos)
			blockEntity.storedBlock = context.stack.blockData
		}
		
		return result
	}
	
	override fun useOnBlock(context: ItemUsageContext?): ActionResult {
		if (context!!.stack.hasBlockData) return ActionResult.PASS
		
		val success = EnderBoxBlock.wrapBlock(context.world, context.blockPos, EnderBoxMod.enderBoxBlock.defaultState)
		
		if (success && !context.player!!.isCreative) {
			context.stack.decrement(1)
		}
		
		return if (success) ActionResult.SUCCESS else ActionResult.PASS
	}
}

val ItemStack.hasBlockData
	get() = orCreateTag.contains(blockDataKey)

val ItemStack.blockData
	get() = BlockData(orCreateTag.getCompound(blockDataKey))

val Item.identifier
	get() = Registry.ITEM.getId(this)
