package enderbox

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView

private const val storedBlockKey = "storedBlock"

class EnderBoxBlockEntity : BlockEntity(EnderBoxMod.enderBoxBlockEntity), BlockEntityClientSerializable {
	companion object {
		fun get(blockView: BlockView, pos: BlockPos) = blockView.getBlockEntity(pos) as EnderBoxBlockEntity
	}
	
	var storedBlock = BlockData() // boxes are never empty, but this avoids crashing if you place them using commands
	
	override fun fromTag(tag: CompoundTag) {
		super.fromTag(tag)
		storedBlock = BlockData(tag.getCompound(storedBlockKey))
	}
	
	override fun fromClientTag(tag: CompoundTag) {
		storedBlock = BlockData(tag.getCompound(storedBlockKey))
	}
	
	override fun toTag(tag: CompoundTag): CompoundTag = super.toTag(tag).apply {
		put(storedBlockKey, storedBlock.toTag())
	}
	
	override fun toClientTag(tag: CompoundTag): CompoundTag = tag.apply {
		put(storedBlockKey, storedBlock.toTag())
	}
}
