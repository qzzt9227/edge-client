package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.player.copynbt.CopyNbtHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 注入 MultiPlayerGameMode，在创造模式拾取方块/实体时支持自动携带 NBT 数据。
 */
@Mixin(MultiPlayerGameMode.class)
abstract class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyVariable(
		method = "handlePickItemFromBlock",
		at = @At("HEAD"),
		argsOnly = true
	)
	private boolean edgeClient$modifyPickBlockIncludeData(boolean includeData, BlockPos pos) {
		return CopyNbtHooks.shouldIncludeBlockData(minecraft, pos, includeData);
	}

	@ModifyVariable(
		method = "handlePickItemFromEntity",
		at = @At("HEAD"),
		argsOnly = true
	)
	private boolean edgeClient$modifyPickEntityIncludeData(boolean includeData, Entity entity) {
		return CopyNbtHooks.shouldIncludeEntityData(minecraft, entity, includeData);
	}
}
