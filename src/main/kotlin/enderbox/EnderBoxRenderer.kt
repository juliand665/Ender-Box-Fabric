package enderbox

import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack

class EnderBoxRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<EnderBoxBlockEntity>(dispatcher) {
	override fun render(
		blockEntity: EnderBoxBlockEntity, tickDelta: Float,
		matrices: MatrixStack, vertexConsumers: VertexConsumerProvider,
		light: Int, overlay: Int
	) {
		blockEntity.storedBlock.render(false, matrices, vertexConsumers, light, overlay)
	}
	
	companion object {
		fun render(
			itemStack: ItemStack,
			matrices: MatrixStack, vertexConsumers: VertexConsumerProvider,
			light: Int, overlay: Int
		) {
			matrices.push()
			
			val client = MinecraftClient.getInstance()
			
			val isRenderingWorld = (client.worldRenderer as WorldRendererStatus).isRendering
			client.blockRenderManager.renderBlockAsEntity(
				EnderBoxMod.humanEnderBoxBlock.defaultState
					.with(EnderBoxBlock.transparent, !isRenderingWorld && itemStack.blockData != null),
				matrices, vertexConsumers, light, overlay
			)
			
			itemStack.blockData?.let { blockData ->
				BlockEntityRenderDispatcher.INSTANCE.renderEntity(
					EnderBoxBlockEntity().apply { storedBlock = blockData },
					matrices, vertexConsumers, light, overlay
				)
			}
			
			matrices.pop()
		}
	}
}

fun BlockData.render(
	asItem: Boolean,
	matrices: MatrixStack, vertexConsumers: VertexConsumerProvider,
	light: Int, overlay: Int
) {
	matrices.push()
	matrices.translate(0.5, 0.5, 0.5) // center
	val scale = 0.9f
	matrices.scale(scale, scale, scale) // scale centered
	matrices.translate(-0.5, -0.5, -0.5) // translate back to correct origin for block rendering
	
	val renderBlockState = { blockState: BlockState ->
		MinecraftClient.getInstance().blockRenderManager.renderBlockAsEntity(
			blockState, matrices, vertexConsumers, light, overlay
		)
	}
	
	if (asItem) {
		renderBlockState(blockState)
	} else {
		render(
			{
				BlockEntityRenderDispatcher.INSTANCE.renderEntity(
					it, matrices, vertexConsumers, light, overlay
				)
			},
			renderBlockState
		)
	}
	
	matrices.pop()
}

interface WorldRendererStatus {
	val isRendering: Boolean
}
