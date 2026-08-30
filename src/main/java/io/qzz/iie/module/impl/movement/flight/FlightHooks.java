package io.qzz.iie.module.impl.movement.flight;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;

import java.util.Objects;

/**
 * 飞行模块桥接与数据包拦截处理器。
 *
 * <p>负责在飞行期间将向服务端发送的移动数据包伪装为着地状态（{@code onGround = true}），
 * 并拦截未经服务端允许的飞行能力数据包，避免触发反作弊或非飞行服务端踢出。</p>
 */
public final class FlightHooks {
	private static volatile FlightModule installedModule;

	private FlightHooks() {
	}

	public static void install(FlightModule module) {
		installedModule = Objects.requireNonNull(module, "module");
	}

	public static void uninstall(FlightModule module) {
		if (installedModule == module) {
			installedModule = null;
		}
	}

	public static boolean isEnabled() {
		FlightModule module = installedModule;
		return module != null && module.isEnabled();
	}

	public static boolean shouldSpoofGround() {
		FlightModule module = installedModule;
		return module != null && module.isEnabled() && module.spoofGround().value();
	}

	/**
	 * 对客户端向服务端发送的数据包进行过滤与状态伪装。
	 *
	 * @param packet 原始出站数据包
	 * @return 处理后的数据包（若为 {@code null} 则丢弃该数据包）
	 */
	public static Packet<?> processOutgoingPacket(Packet<?> packet) {
		if (!isEnabled()) {
			return packet;
		}

		// 拦截能力同步数据包，防止服务端因生存模式下收到 flying=true 而判定非法
		if (packet instanceof ServerboundPlayerAbilitiesPacket abilitiesPacket) {
			if (abilitiesPacket.isFlying()) {
				return null;
			}
		}

		// 地面状态伪装
		if (packet instanceof ServerboundMovePlayerPacket movePacket) {
			if (shouldSpoofGround() && !movePacket.isOnGround()) {
				if (movePacket instanceof ServerboundMovePlayerPacket.PosRot posRot) {
					return new ServerboundMovePlayerPacket.PosRot(
						posRot.getX(0),
						posRot.getY(0),
						posRot.getZ(0),
						posRot.getYRot(0),
						posRot.getXRot(0),
						true,
						posRot.horizontalCollision()
					);
				} else if (movePacket instanceof ServerboundMovePlayerPacket.Pos pos) {
					return new ServerboundMovePlayerPacket.Pos(
						pos.getX(0),
						pos.getY(0),
						pos.getZ(0),
						true,
						pos.horizontalCollision()
					);
				} else if (movePacket instanceof ServerboundMovePlayerPacket.Rot rot) {
					return new ServerboundMovePlayerPacket.Rot(
						rot.getYRot(0),
						rot.getXRot(0),
						true,
						rot.horizontalCollision()
					);
				} else if (movePacket instanceof ServerboundMovePlayerPacket.StatusOnly statusOnly) {
					return new ServerboundMovePlayerPacket.StatusOnly(
						true,
						statusOnly.horizontalCollision()
					);
				}
			}
		}

		return packet;
	}
}
