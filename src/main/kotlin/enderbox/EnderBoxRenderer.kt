package enderbox

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import kotlin.math.PI

class EnderBoxRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<EnderBoxBlockEntity>(dispatcher) {
	override fun render(blockEntity: EnderBoxBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
		matrices.push()
		matrices.translate(0.5, 0.5, 0.5) // center
		
		val blockData = blockEntity.storedBlock
		blockData.render(matrices, vertexConsumers, light, overlay)
		
		matrices.pop()
	}
}

fun BlockData.render(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
	val client = MinecraftClient.getInstance()
	
	val cycleLength = 5000 // milliseconds
	val time = Util.getMeasuringTimeMs()
	val angle = 2 * PI.toFloat() * (time % cycleLength) / cycleLength
	matrices.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(angle))
	
	val renderer = client.itemRenderer
	val blockStack = pickedBlock(client.world!!, BlockPos.ORIGIN)
	val model = renderer.getHeldItemModel(blockStack, null, null)
	
	val scale = if (model.is2DModel) 0.6f else 1.2f
	matrices.scale(scale, scale, scale)
	renderer.renderItem(blockStack, ModelTransformation.Mode.FIXED, light, overlay, matrices, vertexConsumers)
}

val BakedModel.is2DModel: Boolean
	get() = false