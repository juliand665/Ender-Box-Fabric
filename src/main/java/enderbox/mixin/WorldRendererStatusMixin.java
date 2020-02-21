package enderbox.mixin;

import enderbox.WorldRendererStatus;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererStatusMixin implements WorldRendererStatus {
	private boolean isRendering = false;
	
	@Override
	public boolean isRendering() {
		return isRendering;
	}
	
	@Inject(at = @At("HEAD"), method = "render")
	private void onRenderStart(CallbackInfo callbackInfo) {
		isRendering = true;
	}
	
	@Inject(at = @At("RETURN"), method = "render")
	private void onRenderEnd(CallbackInfo callbackInfo) {
		isRendering = false;
	}
}
