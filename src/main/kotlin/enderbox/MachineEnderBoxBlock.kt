package enderbox

import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView

class MachineEnderBoxBlock(settings: Settings) : EnderBoxBlock(settings) {
	override fun getPickStack(blockView: BlockView, pos: BlockPos, blockState: BlockState): ItemStack = ItemStack.EMPTY
}