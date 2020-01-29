package enderbox

import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import java.util.function.Supplier

object EnderBoxMod {
	const val modID = "enderbox"
	
	val logger = LogManager.getLogger()
	
	val enderBoxBlock = EnderBoxBlock(
		FabricBlockSettings
			.of(enderBoxMaterial)
			.resistance(100F)
			.breakByHand(true)
			.breakInstantly()
			.build()
	)
	
	val enderBoxItem = EnderBoxItem(Item.Settings().group(ItemGroup.MISC))
	
	val enderBoxBlockEntity: BlockEntityType<EnderBoxBlockEntity> = BlockEntityType.Builder
		.create(Supplier(::EnderBoxBlockEntity), enderBoxBlock)
		.build(null)
	
	@Suppress("unused")
	fun initialize() {
		Registry.register(Registry.BLOCK, identifier("ender_box"), enderBoxBlock)
		Registry.register(Registry.ITEM, identifier("ender_box"), enderBoxItem)
		Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier("ender_box"), enderBoxBlockEntity)
	}
	
	@Suppress("unused")
	fun initializeClient() {
		BlockEntityRendererRegistry.INSTANCE.register(enderBoxBlockEntity, ::EnderBoxRenderer)
		BlockRenderLayerMap.INSTANCE.putBlock(enderBoxBlock, RenderLayer.getTranslucent())
	}
	
	fun identifier(path: String) = Identifier(modID, path)
}
