package enderbox

import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.block.FabricMaterialBuilder
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.minecraft.block.Material
import net.minecraft.block.MaterialColor
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Supplier

object EnderBoxMod {
	const val modID = "enderbox"
	
	val logger: Logger = LogManager.getLogger()
	
	private val enderBoxMaterial: Material = FabricMaterialBuilder(MaterialColor.BLACK)
		.lightPassesThrough()
		.notSolid()
		.build()
	
	val humanEnderBoxBlock = HumanEnderBoxBlock(
		FabricBlockSettings
			.of(enderBoxMaterial)
			.breakByHand(true)
			.breakInstantly()
			.resistance(100F) // it's made of obsidian ok
			.build()
	)
	
	val machineEnderBoxBlock = MachineEnderBoxBlock(
		FabricBlockSettings
			.of(enderBoxMaterial)
			.hardness(-1F)
			.resistance(1000F) // would be a sssshame
			.dropsNothing()
			.build()
	)
	
	val enderBoxItem = EnderBoxItem(Item.Settings().group(ItemGroup.MISC))
	
	val enderBoxerBlock = EnderBoxerBlock(
		FabricBlockSettings
			.of(Material.STONE)
			.strength(3F, 10F)
			.build()
	)
	
	val enderBoxerItem = EnderBoxerItem(Item.Settings().group(ItemGroup.MISC))
	
	val enderBoxBlockEntity: BlockEntityType<EnderBoxBlockEntity> = BlockEntityType.Builder
		.create(Supplier(::EnderBoxBlockEntity), humanEnderBoxBlock, machineEnderBoxBlock)
		.build(null)
	
	@Suppress("unused")
	fun initialize() {
		Registry.register(Registry.BLOCK, identifier("ender_box"), humanEnderBoxBlock)
		Registry.register(Registry.BLOCK, identifier("ender_boxed"), machineEnderBoxBlock)
		Registry.register(Registry.BLOCK, identifier("ender_boxer"), enderBoxerBlock)
		
		Registry.register(Registry.ITEM, identifier("ender_box"), enderBoxItem)
		Registry.register(Registry.ITEM, identifier("ender_boxer"), enderBoxerItem)
		
		Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier("ender_box"), enderBoxBlockEntity)
	}
	
	@Suppress("unused")
	fun initializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(humanEnderBoxBlock, RenderLayer.getTranslucent())
		BlockRenderLayerMap.INSTANCE.putBlock(machineEnderBoxBlock, RenderLayer.getTranslucent())
		BlockEntityRendererRegistry.INSTANCE.register(enderBoxBlockEntity, ::EnderBoxRenderer)
	}
	
	fun identifier(path: String) = Identifier(modID, path)
}
