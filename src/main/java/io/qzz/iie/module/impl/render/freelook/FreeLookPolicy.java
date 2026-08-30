package io.qzz.iie.module.impl.render.freelook;

/**
 * 自由视角核心旋转与插值计算策略（纯 Java 领域模型，无 Minecraft 依赖）。
 */
public final class FreeLookPolicy {
	private float cameraYaw;
	private float cameraPitch;
	private float prevCameraYaw;
	private float prevCameraPitch;
	private boolean active;

	public FreeLookPolicy() {
	}

	/**
	 * 当自由视角激活时，用玩家当前的初始视角初始化相机角度。
	 */
	public void activate(float initialYaw, float initialPitch) {
		this.cameraYaw = initialYaw;
		this.cameraPitch = initialPitch;
		this.prevCameraYaw = initialYaw;
		this.prevCameraPitch = initialPitch;
		this.active = true;
	}

	/**
	 * 当自由视角关闭时重置激活状态。
	 */
	public void deactivate() {
		this.active = false;
	}

	public boolean isActive() {
		return active;
	}

	public float cameraYaw() {
		return cameraYaw;
	}

	public float cameraPitch() {
		return cameraPitch;
	}

	public float prevCameraYaw() {
		return prevCameraYaw;
	}

	public float prevCameraPitch() {
		return prevCameraPitch;
	}

	/**
	 * 在客户端 Tick 时记录上一帧角度，用于平滑渲染插值。
	 */
	public void tick() {
		this.prevCameraYaw = this.cameraYaw;
		this.prevCameraPitch = this.cameraPitch;
	}

	/**
	 * 累加鼠标旋转。
	 *
	 * @param deltaYaw 水平偏移（度）
	 * @param deltaPitch 垂直偏移（度）
	 * @param invertYaw 是否反转水平方向
	 * @param invertPitch 是否反转垂直方向
	 */
	public void turn(double deltaYaw, double deltaPitch, boolean invertYaw, boolean invertPitch) {
		if (!active) {
			return;
		}
		float dy = (float) (deltaYaw * 0.15);
		float dp = (float) (deltaPitch * 0.15);

		if (invertYaw) {
			dy = -dy;
		}
		if (invertPitch) {
			dp = -dp;
		}

		this.cameraYaw += dy;
		this.cameraPitch = Math.clamp(this.cameraPitch + dp, -90.0f, 90.0f);
	}

	/**
	 * 计算带有 partialTicks 的渲染 Yaw 角度。
	 */
	public float renderYaw(float partialTicks) {
		return prevCameraYaw + (cameraYaw - prevCameraYaw) * partialTicks;
	}

	/**
	 * 计算带有 partialTicks 的渲染 Pitch 角度。
	 */
	public float renderPitch(float partialTicks) {
		return prevCameraPitch + (cameraPitch - prevCameraPitch) * partialTicks;
	}
}
