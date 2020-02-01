package enderbox

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

class EnderBoxerItem(settings: Settings) : BlockItem(EnderBoxMod.enderBoxerBlock, settings) {
	override fun appendTooltip(itemStack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
		super.appendTooltip(itemStack, world, tooltip, context)
		
		val id = Registry.ITEM.getId(this)
		
		tooltip.add(localized("tooltip", id, "default"))
	}
}
