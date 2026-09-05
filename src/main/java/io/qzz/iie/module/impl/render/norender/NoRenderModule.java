package io.qzz.iie.module.impl.render.norender;

import io.qzz.iie.module.Module;
import io.qzz.iie.module.ModuleId;
import io.qzz.iie.module.ModuleMetadata;
import io.qzz.iie.setting.BooleanSetting;
import io.qzz.iie.setting.DoubleSetting;
import io.qzz.iie.setting.FoldSetting;
import io.qzz.iie.setting.KeybindSetting;

/**
 * 无渲染与防卡顿增强模块。
 *
 * <p>按分组组织：粒子效果、静态实体与界面、物效、动画四大折叠大类，
 * 支持动态全粒子列表管理、多维度迷雾、材质动画冻结与各类视觉杂项屏蔽。</p>
 */
public final class NoRenderModule extends Module {

	// ================= 1. 粒子效果 (Particles) =================
	private final FoldSetting particlesGroup = setting(new FoldSetting(
		"group_particles",
		"client.setting.no_render.group.particles",
		false
	));

	private final ParticleBlacklistSetting particleCustomBlacklist = setting(new ParticleBlacklistSetting(
		"particle_custom_blacklist",
		"client.setting.no_render.particle_custom_blacklist"
	).indent(1).visibleWhen(particlesGroup::value));

	private final BooleanSetting particleEnvironment = setting(new BooleanSetting(
		"particle_environment",
		"client.setting.no_render.particle_environment",
		false
	).indent(1).visibleWhen(particlesGroup::value));

	private final BooleanSetting particleVillager = setting(new BooleanSetting(
		"particle_villager",
		"client.setting.no_render.particle_villager",
		false
	).indent(1).visibleWhen(particlesGroup::value));

	private final BooleanSetting particleComposter = setting(new BooleanSetting(
		"particle_composter",
		"client.setting.no_render.particle_composter",
		false
	).indent(1).visibleWhen(particlesGroup::value));

	private final BooleanSetting particleRain = setting(new BooleanSetting(
		"particle_rain",
		"client.setting.no_render.particle_rain",
		false
	).indent(1).visibleWhen(particlesGroup::value));

	private final BooleanSetting particleBlockBreak = setting(new BooleanSetting(
		"particle_block_break",
		"client.setting.no_render.particle_block_break",
		false
	).indent(1).visibleWhen(particlesGroup::value));

	private final BooleanSetting particleExplosion = setting(new BooleanSetting(
		"particle_explosion",
		"client.setting.no_render.particle_explosion",
		false
	).indent(1).visibleWhen(particlesGroup::value));

	// ================= 2. 静态实体与界面 (Static Entities & UI) =================
	private final FoldSetting staticUiGroup = setting(new FoldSetting(
		"group_static_ui",
		"client.setting.no_render.group.static_ui",
		false
	));

	// 2.1 静态实体 (Static Entities)
	private final FoldSetting subgroupStaticEntities = setting(new FoldSetting(
		"subgroup_static_entities",
		"client.setting.no_render.subgroup.static_entities",
		false
	).indent(1).visibleWhen(staticUiGroup::value));

	private final BooleanSetting itemFrames = setting(new BooleanSetting(
		"item_frames",
		"client.setting.no_render.item_frames",
		false
	).indent(2).visibleWhen(() -> staticUiGroup.value() && subgroupStaticEntities.value()));

	private final BooleanSetting armorStands = setting(new BooleanSetting(
		"armor_stands",
		"client.setting.no_render.armor_stands",
		false
	).indent(2).visibleWhen(() -> staticUiGroup.value() && subgroupStaticEntities.value()));

	private final BooleanSetting paintings = setting(new BooleanSetting(
		"paintings",
		"client.setting.no_render.paintings",
		false
	).indent(2).visibleWhen(() -> staticUiGroup.value() && subgroupStaticEntities.value()));

	// 2.2 界面元素 (UI Elements)
	private final FoldSetting subgroupUiElements = setting(new FoldSetting(
		"subgroup_ui_elements",
		"client.setting.no_render.subgroup.ui_elements",
		false
	).indent(1).visibleWhen(staticUiGroup::value));

	private final BooleanSetting itemFrameNameTags = setting(new BooleanSetting(
		"item_frame_name_tags",
		"client.setting.no_render.item_frame_name_tags",
		false
	).indent(2).visibleWhen(() -> staticUiGroup.value() && subgroupUiElements.value()));

	private final BooleanSetting playerNameTags = setting(new BooleanSetting(
		"player_name_tags",
		"client.setting.no_render.player_name_tags",
		false
	).indent(2).visibleWhen(() -> staticUiGroup.value() && subgroupUiElements.value()));

	// 2.3 其他渲染对象 (Other Renderings)
	private final FoldSetting subgroupOtherRenderings = setting(new FoldSetting(
		"subgroup_other_renderings",
		"client.setting.no_render.subgroup.other_renderings",
		false
	).indent(1).visibleWhen(staticUiGroup::value));

	private final BooleanSetting beaconBeams = setting(new BooleanSetting(
		"beacon_beams",
		"client.setting.no_render.beacon_beams",
		false
	).indent(2).visibleWhen(() -> staticUiGroup.value() && subgroupOtherRenderings.value()));

	private final BooleanSetting enchantingTableBooks = setting(new BooleanSetting(
		"enchanting_table_books",
		"client.setting.no_render.enchanting_table_books",
		false
	).indent(2).visibleWhen(() -> staticUiGroup.value() && subgroupOtherRenderings.value()));

	private final BooleanSetting movingPistons = setting(new BooleanSetting(
		"moving_pistons",
		"client.setting.no_render.moving_pistons",
		false
	).indent(2).visibleWhen(() -> staticUiGroup.value() && subgroupOtherRenderings.value()));

	// 2.4 视野遮挡
	private final BooleanSetting underwaterLavaOverlay = setting(new BooleanSetting(
		"underwater_lava_overlay",
		"client.setting.no_render.underwater_lava_overlay",
		false
	).indent(1).visibleWhen(staticUiGroup::value));

	// ================= 3. 物效 (Effects) =================
	private final FoldSetting effectsGroup = setting(new FoldSetting(
		"group_effects",
		"client.setting.no_render.group.effects",
		false
	));

	// 3.1 迷雾 (Fog)
	private final FoldSetting subgroupFog = setting(new FoldSetting(
		"subgroup_fog",
		"client.setting.no_render.subgroup.fog",
		false
	).indent(1).visibleWhen(effectsGroup::value));

	private final DoubleSetting globalFogDistance = setting(new DoubleSetting(
		"global_fog_distance",
		"client.setting.no_render.global_fog_distance",
		1.0,
		1.0,
		10.0,
		0.5
	).indent(2).visibleWhen(() -> effectsGroup.value() && subgroupFog.value()));

	private final BooleanSetting fogOverworld = setting(new BooleanSetting(
		"fog_overworld",
		"client.setting.no_render.fog_overworld",
		false
	).indent(2).visibleWhen(() -> effectsGroup.value() && subgroupFog.value()));

	private final BooleanSetting fogNether = setting(new BooleanSetting(
		"fog_nether",
		"client.setting.no_render.fog_nether",
		false
	).indent(2).visibleWhen(() -> effectsGroup.value() && subgroupFog.value()));

	private final BooleanSetting fogEnd = setting(new BooleanSetting(
		"fog_end",
		"client.setting.no_render.fog_end",
		false
	).indent(2).visibleWhen(() -> effectsGroup.value() && subgroupFog.value()));

	// 3.2 天空与天气 (Sky & Weather)
	private final FoldSetting subgroupSkyWeather = setting(new FoldSetting(
		"subgroup_sky_weather",
		"client.setting.no_render.subgroup.sky_weather",
		false
	).indent(1).visibleWhen(effectsGroup::value));

	private final BooleanSetting sky = setting(new BooleanSetting(
		"sky",
		"client.setting.no_render.sky",
		false
	).indent(2).visibleWhen(() -> effectsGroup.value() && subgroupSkyWeather.value()));

	private final BooleanSetting weather = setting(new BooleanSetting(
		"weather",
		"client.setting.no_render.weather",
		false
	).indent(2).visibleWhen(() -> effectsGroup.value() && subgroupSkyWeather.value()));

	private final BooleanSetting biomeColors = setting(new BooleanSetting(
		"biome_colors",
		"client.setting.no_render.biome_colors",
		false
	).indent(2).visibleWhen(() -> effectsGroup.value() && subgroupSkyWeather.value()));

	private final BooleanSetting skyColors = setting(new BooleanSetting(
		"sky_colors",
		"client.setting.no_render.sky_colors",
		false
	).indent(2).visibleWhen(() -> effectsGroup.value() && subgroupSkyWeather.value()));

	// 3.3 光照更新 (Light Updates)
	private final BooleanSetting lightUpdates = setting(new BooleanSetting(
		"light_updates",
		"client.setting.no_render.light_updates",
		false
	).indent(1).visibleWhen(effectsGroup::value));

	// ================= 4. 动画 (Animations) =================
	private final FoldSetting animationsGroup = setting(new FoldSetting(
		"group_animations",
		"client.setting.no_render.group.animations",
		false
	));

	private final BooleanSetting animationWater = setting(new BooleanSetting(
		"animation_water",
		"client.setting.no_render.animation_water",
		false
	).indent(1).visibleWhen(animationsGroup::value));

	private final BooleanSetting animationLava = setting(new BooleanSetting(
		"animation_lava",
		"client.setting.no_render.animation_lava",
		false
	).indent(1).visibleWhen(animationsGroup::value));

	private final BooleanSetting animationFire = setting(new BooleanSetting(
		"animation_fire",
		"client.setting.no_render.animation_fire",
		false
	).indent(1).visibleWhen(animationsGroup::value));

	private final BooleanSetting animationPortals = setting(new BooleanSetting(
		"animation_portals",
		"client.setting.no_render.animation_portals",
		false
	).indent(1).visibleWhen(animationsGroup::value));

	private final BooleanSetting animationBlocks = setting(new BooleanSetting(
		"animation_blocks",
		"client.setting.no_render.animation_blocks",
		false
	).indent(1).visibleWhen(animationsGroup::value));

	private final BooleanSetting animationSculkSensors = setting(new BooleanSetting(
		"animation_sculk_sensors",
		"client.setting.no_render.animation_sculk_sensors",
		false
	).indent(1).visibleWhen(animationsGroup::value));

	private final KeybindSetting shortcut = keybind(new KeybindSetting(
		"keybind",
		"client.setting.module_keybind"
	));

	public NoRenderModule() {
		super(new ModuleMetadata(
			ModuleId.of("client", "no_render"),
			"client.module.no_render.name",
			"client.module.no_render.description",
			310
		));
	}

	// Group 1 Getters
	public FoldSetting particlesGroup() { return particlesGroup; }
	public ParticleBlacklistSetting particleCustomBlacklist() { return particleCustomBlacklist; }
	public BooleanSetting particleEnvironment() { return particleEnvironment; }
	public BooleanSetting particleVillager() { return particleVillager; }
	public BooleanSetting particleComposter() { return particleComposter; }
	public BooleanSetting particleRain() { return particleRain; }
	public BooleanSetting particleBlockBreak() { return particleBlockBreak; }
	public BooleanSetting particleExplosion() { return particleExplosion; }

	// Group 2 Getters
	public FoldSetting staticUiGroup() { return staticUiGroup; }
	public FoldSetting subgroupStaticEntities() { return subgroupStaticEntities; }
	public BooleanSetting itemFrames() { return itemFrames; }
	public BooleanSetting armorStands() { return armorStands; }
	public BooleanSetting paintings() { return paintings; }
	public FoldSetting subgroupUiElements() { return subgroupUiElements; }
	public BooleanSetting itemFrameNameTags() { return itemFrameNameTags; }
	public BooleanSetting playerNameTags() { return playerNameTags; }
	public FoldSetting subgroupOtherRenderings() { return subgroupOtherRenderings; }
	public BooleanSetting beaconBeams() { return beaconBeams; }
	public BooleanSetting enchantingTableBooks() { return enchantingTableBooks; }
	public BooleanSetting movingPistons() { return movingPistons; }
	public BooleanSetting underwaterLavaOverlay() { return underwaterLavaOverlay; }

	// Group 3 Getters
	public FoldSetting effectsGroup() { return effectsGroup; }
	public FoldSetting subgroupFog() { return subgroupFog; }
	public DoubleSetting globalFogDistance() { return globalFogDistance; }
	public BooleanSetting fogOverworld() { return fogOverworld; }
	public BooleanSetting fogNether() { return fogNether; }
	public BooleanSetting fogEnd() { return fogEnd; }
	public FoldSetting subgroupSkyWeather() { return subgroupSkyWeather; }
	public BooleanSetting sky() { return sky; }
	public BooleanSetting weather() { return weather; }
	public BooleanSetting biomeColors() { return biomeColors; }
	public BooleanSetting skyColors() { return skyColors; }
	public BooleanSetting lightUpdates() { return lightUpdates; }

	// Group 4 Getters
	public FoldSetting animationsGroup() { return animationsGroup; }
	public BooleanSetting animationWater() { return animationWater; }
	public BooleanSetting animationLava() { return animationLava; }
	public BooleanSetting animationFire() { return animationFire; }
	public BooleanSetting animationPortals() { return animationPortals; }
	public BooleanSetting animationBlocks() { return animationBlocks; }
	public BooleanSetting animationSculkSensors() { return animationSculkSensors; }
}
