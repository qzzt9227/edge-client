package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.render.norender.NoRenderHooks;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
abstract class BiomeColorsMixin {

	@Inject(
		method = "getAverageGrassColor(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void edgeClient$onGetAverageGrassColor(
		BlockAndTintGetter blockAndTintGetter,
		BlockPos blockPos,
		CallbackInfoReturnable<Integer> cir
	) {
		if (NoRenderHooks.shouldNoRenderBiomeColors()) {
			cir.setReturnValue(0x7CBD6B);
		}
	}

	@Inject(
		method = "getAverageFoliageColor(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void edgeClient$onGetAverageFoliageColor(
		BlockAndTintGetter blockAndTintGetter,
		BlockPos blockPos,
		CallbackInfoReturnable<Integer> cir
	) {
		if (NoRenderHooks.shouldNoRenderBiomeColors()) {
			cir.setReturnValue(0x48B518);
		}
	}

	@Inject(
		method = "getAverageWaterColor(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void edgeClient$onGetAverageWaterColor(
		BlockAndTintGetter blockAndTintGetter,
		BlockPos blockPos,
		CallbackInfoReturnable<Integer> cir
	) {
		if (NoRenderHooks.shouldNoRenderBiomeColors()) {
			cir.setReturnValue(0x3F76E4);
		}
	}
}
