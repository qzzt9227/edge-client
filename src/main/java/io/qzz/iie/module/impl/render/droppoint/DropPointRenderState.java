package io.qzz.iie.module.impl.render.droppoint;

/** Immutable data copied during extraction and consumed by the world drawing phase. */
public record DropPointRenderState(int x, int y, int z, DropPointColor color) {
}
