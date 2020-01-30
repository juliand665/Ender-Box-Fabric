package enderbox

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
		matrices.push()
		matrices.translate(0.5, 0.5, 0.5) // center
		
		blockEntity.storedBlock.render(matrices, vertexConsumers, light, overlay)
		
		matrices.pop()
	}
	
	companion object {
		fun render(
			itemStack: ItemStack,
			matrices: MatrixStack, vertexConsumers: VertexConsumerProvider,
			light: Int, overlay: Int
		) {
			matrices.push()
			
			MinecraftClient.getInstance().blockRenderManager.renderBlockAsEntity(
				EnderBoxMod.enderBoxBlock.defaultState,
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
	matrices: MatrixStack, vertexConsumers: VertexConsumerProvider,
	light: Int, overlay: Int
) {
	matrices.push()
	val scale = 0.9f
	matrices.scale(scale, scale, scale)
	
	// scale was centered; now translate back to correct origin for block rendering
	matrices.translate(-0.5, -0.5, -0.5)
	MinecraftClient.getInstance().blockRenderManager.renderBlockAsEntity(
		blockState,
		matrices, vertexConsumers, light, overlay
	)
	matrices.pop()
}