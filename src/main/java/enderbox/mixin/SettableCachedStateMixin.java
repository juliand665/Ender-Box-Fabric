package enderbox.mixin;

import enderbox.SettableCachedState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class SettableCachedStateMixin implements SettableCachedState {
	@Shadow
	@Nullable
	private BlockState cachedState;
	
	public void setCachedState(@Nullable BlockState cachedState) {
		this.cachedState = cachedState;
	}
}
