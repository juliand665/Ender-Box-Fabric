package enderbox

import com.google.common.collect.ImmutableMap
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView

private const val blockStateKey = "blockState"
private const val blockEntityTagKey = "blockEntityTag"

data class BlockData(
	var blockState: BlockState = BlockState(Blocks.AIR, ImmutableMap.of()),
	var blockEntityTag: CompoundTag? = null,
	var cachedBlockEntity: BlockEntity? = null
) {
	val block: Block
		get() = blockState.block
	
	constructor(tag: CompoundTag) : this(
		NbtHelper.toBlockState(tag.getCompound(blockStateKey)),
		if (tag.contains(blockEntityTagKey)) tag.getCompound(blockEntityTagKey) else null
	)
	
	fun toTag(tag: CompoundTag = CompoundTag()): CompoundTag {
		tag.put(blockStateKey, NbtHelper.fromBlockState(blockState))
		
		tag.remove(blockEntityTagKey)
		if (blockEntityTag != null) {
			tag.put(blockEntityTagKey, blockEntityTag)
		}
		
		return tag
	}
	
	/** updates the contained tile entity's position, if applicable  */
	fun updatePosition(position: BlockPos) {
		blockEntityTag?.apply {
			// this is kinda hacky, but there's no other option
			putInt("x", position.x)
			putInt("y", position.y)
			putInt("z", position.z)
		}
	}
	
	fun pickedBlock(blockView: BlockView, pos: BlockPos): ItemStack {
		try {
			// try to simulate picking the block
			return block.getPickStack(blockView, pos, blockState)
		} catch (_: Exception) {
			// could fail e.g. because the pick block code expects a certain tile entity
		}
		
		// construct the dropped item ourselves
		return ItemStack(blockState.block, 1)
	}
	
	fun blockEntity(blockView: BlockView): BlockEntity? = cachedBlockEntity
		?: blockEntityTag?.let {
			(block as? BlockWithEntity)
				?.createBlockEntity(blockView)
				?.apply { fromTag(it) }
		}
}
