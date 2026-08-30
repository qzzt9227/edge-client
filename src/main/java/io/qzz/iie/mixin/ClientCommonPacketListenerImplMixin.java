package io.qzz.iie.mixin;

import io.qzz.iie.module.impl.movement.flight.FlightHooks;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截出站网络数据包，处理飞行能力包抑制与地面状态伪装。
 */
@Mixin(ClientCommonPacketListenerImpl.class)
abstract class ClientCommonPacketListenerImplMixin {
	@Shadow
	public abstract void send(Packet<?> packet);

	@Unique
	private boolean edgeClient$handlingFlightPacket;

	@Inject(method = "send", at = @At("HEAD"), cancellable = true)
	private void edgeClient$onSendPacket(Packet<?> packet, CallbackInfo ci) {
		if (edgeClient$handlingFlightPacket) {
			return;
		}

		Packet<?> processed = FlightHooks.processOutgoingPacket(packet);
		if (processed == null) {
			ci.cancel();
			return;
		}

		if (processed != packet) {
			ci.cancel();
			edgeClient$handlingFlightPacket = true;
			try {
				send(processed);
			} finally {
				edgeClient$handlingFlightPacket = false;
			}
		}
	}
}
