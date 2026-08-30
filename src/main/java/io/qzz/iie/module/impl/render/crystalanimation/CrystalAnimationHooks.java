package io.qzz.iie.module.impl.render.crystalanimation;

import com.mojang.math.Axis;
import net.minecraft.client.model.object.crystal.EndCrystalModel;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;

import java.util.Objects;

/**
 * 桥接 {@link CrystalAnimationModule} 与末地水晶渲染模型的钩子类。
 */
public final class CrystalAnimationHooks {
	private static volatile CrystalAnimationModule module;

	private CrystalAnimationHooks() {
	}

	public static void install(CrystalAnimationModule installedModule) {
		module = Objects.requireNonNull(installedModule, "installedModule");
	}

	public static boolean isEnabled() {
		CrystalAnimationModule current = module;
		return current != null && current.isEnabled();
	}

	public static CrystalAnimationMode mode() {
		CrystalAnimationModule current = module;
		if (current == null || !current.isEnabled()) {
			return null;
		}
		return current.modeSetting().value();
	}

	public static double speed() {
		CrystalAnimationModule current = module;
		if (current == null || !current.isEnabled()) {
			return 1.0;
		}
		return current.speedSetting().value();
	}

	public static double offsetX() {
		CrystalAnimationModule current = module;
		if (current == null || !current.isEnabled()) {
			return 0.0;
		}
		return current.offsetXSetting().value();
	}

	public static double offsetY() {
		CrystalAnimationModule current = module;
		if (current == null || !current.isEnabled()) {
			return 0.0;
		}
		return current.offsetYSetting().value();
	}

	public static double offsetZ() {
		CrystalAnimationModule current = module;
		if (current == null || !current.isEnabled()) {
			return 0.0;
		}
		return current.offsetZSetting().value();
	}

	public static double scale() {
		CrystalAnimationModule current = module;
		if (current == null || !current.isEnabled()) {
			return 1.0;
		}
		return current.scaleSetting().value();
	}

	/**
	 * 应用自定义末地水晶姿态与动画。
	 */
	public static void applyCustomAnim(EndCrystalModel model, EndCrystalRenderState state) {
		if (!isEnabled()) {
			return;
		}
		model.resetPose();
		model.base.visible = state.showsBottom;

		model.outerGlass.x += CrystalAnimationPolicy.toModelUnits(offsetX());
		model.outerGlass.y += CrystalAnimationPolicy.calculateY(offsetY());
		model.outerGlass.z += CrystalAnimationPolicy.toModelUnits(offsetZ());

		float s = (float) scale();
		model.outerGlass.xScale = s;
		model.outerGlass.yScale = s;
		model.outerGlass.zScale = s;

		CrystalAnimationMode currentMode = mode();
		if (currentMode == CrystalAnimationMode.STATIC) {
			// 静止模式：保持 resetPose 的正向方正姿态，不执行任何旋转与浮动
			return;
		}
		if (currentMode == CrystalAnimationMode.SPIN) {
			// 旋转模式：正向水平自旋，无上下浮动
			float spinDegrees = CrystalAnimationPolicy.calculateSpinAngle(state.ageInTicks, speed());
			model.outerGlass.rotateBy(Axis.YP.rotationDegrees(spinDegrees));
			model.innerGlass.rotateBy(Axis.YP.rotationDegrees(-2.0F * spinDegrees));
			model.cube.rotateBy(Axis.YP.rotationDegrees(2.0F * spinDegrees));
		}
	}
}
