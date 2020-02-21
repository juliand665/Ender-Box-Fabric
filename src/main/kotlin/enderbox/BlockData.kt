package enderbox

import com.google.common.collect.ImmutableMap
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtHelper
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView

private const val blockStateKey = "blockState"
private const val blockEntityTagKey = "blockEntityTag"

data class BlockData(
	val blockState: BlockState = BlockState(Blocks.AIR, ImmutableMap.of()),
	val blockEntityTag: CompoundTag? = null,
	private var renderState: RenderState = RenderState.NotLoaded
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
	
	/** client-only! */
	fun render(renderer: (BlockEntity) -> Unit, fallback: (BlockState) -> Unit) {
		if (renderState == RenderState.NotLoaded)
			renderState = makeRenderState()
		
		val blockEntity = (renderState as? RenderState.Loaded)?.blockEntity
		if (blockEntity != null) {
			try {
				renderer(blockEntity)
				return
			} catch (e: Exception) {
				// we probably violated some assumptions; let's just pretend we never tried anything.
				renderState = RenderState.RenderFailed
				fallback(blockState)
			}
		} else {
			fallback(blockState)
		}
	}
	
	private fun makeRenderState(): RenderState {
		if (blockState.renderType != BlockRenderType.ENTITYBLOCK_ANIMATED)
			return RenderState.NotApplicable
		
		try {
			val provider = blockState.block as? BlockEntityProvider
				?: return RenderState.LoadFailed
			
			val world = MinecraftClient.getInstance().world
			val blockEntity = provider.createBlockEntity(world)
				?: return RenderState.LoadFailed
			
			blockEntity.setLocation(world, BlockPos.ORIGIN)
			blockEntity.fromTag(blockEntityTag)
			
			(blockEntity as SettableCachedState).setCachedState(blockState)
			
			return RenderState.Loaded(blockEntity)
		} catch (e: Exception) {
			// we probably violated some assumptions; let's just pretend we never tried anything.
			return RenderState.LoadFailed
		}
	}
	
	sealed class RenderState {
		object NotLoaded : RenderState()
		object NotApplicable : RenderState()
		object LoadFailed : RenderState()
		object RenderFailed : RenderState()
		data class Loaded(val blockEntity: BlockEntity) : RenderState()
	}
}

interface SettableCachedState {
	fun setCachedState(cachedState: BlockState?)
}
