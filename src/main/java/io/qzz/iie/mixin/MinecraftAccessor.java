package io.qzz.iie.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Invoker("startAttack")
	boolean invokeStartAttack();

	@Invoker("startUseItem")
	void invokeStartUseItem();

	@Accessor("rightClickDelay")
	void setRightClickDelay(int delay);

	@Accessor("rightClickDelay")
	int getRightClickDelay();
}
