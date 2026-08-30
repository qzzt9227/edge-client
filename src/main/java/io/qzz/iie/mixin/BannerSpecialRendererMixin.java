package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.special.BannerSpecialRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BannerSpecialRenderer.class)
abstract class BannerSpecialRendererMixin {

	@Inject(
		method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/level/block/entity/BannerPatternLayers;",
		at = @At("RETURN"),
		cancellable = true
	)
	private void edgeClient$cancelSpecialBannerPatterns(
		ItemStack stack,
		CallbackInfoReturnable<BannerPatternLayers> cir
	) {
		if (NoRenderHooks.shouldNoRenderBannerPatterns()) {
			cir.setReturnValue(BannerPatternLayers.EMPTY);
		}
	}
}
