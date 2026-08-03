package io.qzz.iie.module.impl.render.fullbright;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.KeybindSetting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Provides client-side full brightness through an infinite night vision effect.
 */
public final class FullbrightModule extends Module {
	private final KeybindSetting shortcut = keybind(
		new KeybindSetting("keybind", "client.setting.module_keybind")
	);
	private LocalPlayer effectOwner;
	private MobEffectInstance appliedEffect;

	public FullbrightModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "fullbright"),
			"client.module.fullbright.name",
			"client.module.fullbright.description",
			100
		));
	}

	@Override
	protected void onEnable() {
		applyNightVisionIfNeeded();
	}

	@Override
	protected void onDisable() {
		removeOwnedNightVision();
	}

	@Override
	protected void onClientTick() {
		applyNightVisionIfNeeded();
	}

	private void applyNightVisionIfNeeded() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != effectOwner) {
			removeOwnedNightVision();
		}
		if (player == null || player.getEffect(MobEffects.NIGHT_VISION) != null) {
			return;
		}

		MobEffectInstance nightVision = new MobEffectInstance(
			MobEffects.NIGHT_VISION,
			MobEffectInstance.INFINITE_DURATION,
			0,
			false,
			false,
			false
		);
		if (player.addEffect(nightVision)) {
			effectOwner = player;
			appliedEffect = nightVision;
		}
	}

	private void removeOwnedNightVision() {
		if (effectOwner != null
			&& effectOwner.getEffect(MobEffects.NIGHT_VISION) == appliedEffect) {
			effectOwner.removeEffect(MobEffects.NIGHT_VISION);
		}
		effectOwner = null;
		appliedEffect = null;
	}
}
