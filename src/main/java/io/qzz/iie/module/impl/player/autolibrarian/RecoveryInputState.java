package io.qzz.iie.module.impl.player.autolibrarian;

/**
 * 可测试的讲台回收移动输入快照。
 */
public final class RecoveryInputState {
	private boolean forward;
	private boolean jump;
	private boolean shift;

	public void apply(boolean forward, boolean jump, boolean shift) {
		this.forward = forward;
		this.jump = jump;
		this.shift = shift;
	}

	public void release() {
		apply(false, false, false);
	}

	public boolean forward() {
		return forward;
	}

	public boolean jump() {
		return jump;
	}

	public boolean shift() {
		return shift;
	}
}
