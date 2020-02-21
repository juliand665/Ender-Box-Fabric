package enderbox.mixin;

import enderbox.EnderBoxMod;
import enderbox.EnderBoxRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
public abstract class ItemRendererCallbackMixin {
	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	private void render(
		ItemStack itemStack,
		MatrixStack matrices, VertexConsumerProvider vertexConsumer,
		int light, int overlay,
		CallbackInfo callbackInfo
	) {
		if (itemStack.getItem() == EnderBoxMod.INSTANCE.getEnderBoxItem()) {
			EnderBoxRenderer.Companion.render(
				itemStack,
				matrices, vertexConsumer, light, overlay
			);
			callbackInfo.cancel();
		}
	}
}
