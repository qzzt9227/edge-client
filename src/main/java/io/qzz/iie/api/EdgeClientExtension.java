package io.qzz.iie.api;

/**
 * Fabric entrypoint implemented by mods that extend Edge Client.
 *
 * <p>Register the implementation under the {@value #ENTRYPOINT_ID}
 * entrypoint in the dependent mod's {@code fabric.mod.json}.</p>
 */
@FunctionalInterface
public interface EdgeClientExtension {
	String ENTRYPOINT_ID = "edge-client";

	void initialize(EdgeClientExtensionContext context);
}
