package io.qzz.iie.module.impl.render.zoom;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * 将版本敏感的 {@code Camera.calculateFov} 与 {@code MouseHandler.turnPlayer}
 * 注入面限制在一个极小的桥接类中。
 *
 * <p>负责管理放大倍率计算、时间插值平滑过渡以及鼠标灵敏度缩放。脱离 Minecraft
 * 亦可进行完整的单元测试。</p>
 */
public final class ZoomHooks {
	private static final long DEFAULT_TRANSITION_DURATION_MS = 150L;
	private static volatile ZoomModule installedModule;
	private static LongSupplier timeSupplier = System::currentTimeMillis;
	private static float startZoom = 1.0f;
	private static float targetZoom = 1.0f;
	private static long transitionStartTime = 0L;
	private static long transitionDurationMs = DEFAULT_TRANSITION_DURATION_MS;

	private ZoomHooks() {
	}

	public static void install(ZoomModule module) {
		installedModule = Objects.requireNonNull(module, "module");
		resetState();
	}

	public static void resetState() {
		startZoom = 1.0f;
		targetZoom = 1.0f;
		transitionStartTime = 0L;
	}

	public static void setTimeSupplierForTesting(LongSupplier supplier) {
		timeSupplier = Objects.requireNonNull(supplier, "supplier");
	}

	public static void resetTimeSupplierForTesting() {
		timeSupplier = System::currentTimeMillis;
	}

	public static void onStateChanged() {
		ZoomModule module = installedModule;
		if (module == null) {
			return;
		}
		float current = getCurrentMultiplier();
		startZoom = current;
		targetZoom = module.isEnabled() ? module.zoomFactor().value().floatValue() : 1.0f;
		transitionStartTime = timeSupplier.getAsLong();
	}

	/**
	 * 获取当前实际渲染应除以的放大倍率（>= 1.0）。
	 */
	public static float getZoomMultiplier(float partialTicks) {
		return getCurrentMultiplier();
	}

	private static float getCurrentMultiplier() {
		ZoomModule module = installedModule;
		if (module == null) {
			return 1.0f;
		}
		if (!module.smoothZoom().value()) {
			return module.isEnabled() ? module.zoomFactor().value().floatValue() : 1.0f;
		}

		long now = timeSupplier.getAsLong();
		long elapsed = now - transitionStartTime;
		if (transitionStartTime == 0L || elapsed >= transitionDurationMs) {
			return targetZoom;
		}
		if (elapsed <= 0) {
			return startZoom;
		}

		float t = (float) elapsed / (float) transitionDurationMs;
		// 平滑步进 smoothstep: 3*t^2 - 2*t^3
		float smoothT = t * t * (3.0f - 2.0f * t);
		return startZoom + (targetZoom - startZoom) * smoothT;
	}

	/**
	 * 获取鼠标灵敏度乘数（<= 1.0）。
	 */
	public static double getSensitivityMultiplier() {
		ZoomModule module = installedModule;
		if (module == null) {
			return 1.0;
		}
		float multiplier = getCurrentMultiplier();
		if (multiplier <= 1.0f || !module.reduceSensitivity().value()) {
			return 1.0;
		}
		return 1.0 / (double) multiplier;
	}
}
