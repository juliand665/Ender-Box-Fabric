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
	
	init {
		println("initialized block entity")
	}
	
	override fun fromTag(tag: CompoundTag) {
		super.fromTag(tag)
		storedBlock = BlockData(tag.getCompound(storedBlockKey))
		println("loaded from tag; stored: ${storedBlock.blockState}")
	}
	
	override fun fromClientTag(tag: CompoundTag) {
		storedBlock = BlockData(tag.getCompound(storedBlockKey))
		println("loaded from client tag; stored: ${storedBlock.blockState}")
	}
	
	override fun toTag(tag: CompoundTag): CompoundTag = super.toTag(tag).apply {
		println("saving to tag; stored: ${storedBlock.blockState}")
		put(storedBlockKey, storedBlock.toTag())
	}
	
	override fun toClientTag(tag: CompoundTag): CompoundTag = tag.apply {
		println("saving to client tag; stored: ${storedBlock.blockState}")
		put(storedBlockKey, storedBlock.toTag())
	}
}
