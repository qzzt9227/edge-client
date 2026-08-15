package io.qzz.iie.module.impl.render.explosionwarning;

public record ExplosionWarningRenderState(
	int entityId,
	ExplosionTargetKind kind,
	double x,
	double y,
	double z,
	double targetY,
	double halfWidth,
	double halfDepth,
	int tntFuse,
	float creeperSwelling
) {
}
