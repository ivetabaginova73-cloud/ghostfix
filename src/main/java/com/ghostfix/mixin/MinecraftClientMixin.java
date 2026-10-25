package com.ghostfix.mixin;

import com.ghostfix.GhostFixClient;
import com.ghostfix.GhostFixConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Attack buffering.
 *
 * Vanilla throws your click away if your crosshair is on nothing for that one tick,
 * and even resets your attack cooldown. When chasing someone (especially in the air)
 * the target often "slips" onto your crosshair a moment later, which feels like a ghost hit.
 *
 * This remembers a missed click for a few ticks and fires it as soon as the crosshair is on
 * a valid target again. Normal vanilla reach is used - reach and hitboxes are NOT changed.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow public HitResult crosshairTarget;
	@Shadow public ClientPlayerEntity player;

	@Shadow
	protected abstract boolean doAttack();

	/** A click that would hit empty air: remember it instead of wasting it. */
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	private void ghostfix$rememberMiss(CallbackInfoReturnable<Boolean> cir) {
		GhostFixConfig cfg = GhostFixConfig.get();
		if (!cfg.enabled || this.player == null || this.player.isSpectator()) {
			return;
		}
		if (this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.MISS) {
			GhostFixClient.bufferLeft = cfg.bufferTicks;
			this.player.swingHand(Hand.MAIN_HAND); // still shows the swing like vanilla
			cir.setReturnValue(false);             // but does not reset your cooldown
		}
	}

	/** Every tick: if a remembered click is waiting and a target is now under the crosshair, attack. */
	@Inject(method = "tick", at = @At("HEAD"))
	private void ghostfix$fireBuffered(CallbackInfo ci) {
		if (GhostFixClient.bufferLeft <= 0) {
			return;
		}
		GhostFixConfig cfg = GhostFixConfig.get();
		if (!cfg.enabled || this.player == null) {
			GhostFixClient.bufferLeft = 0;
			return;
		}
		GhostFixClient.bufferLeft--;

		if (this.crosshairTarget == null || this.crosshairTarget.getType() != HitResult.Type.ENTITY) {
			return; // still nothing there, keep waiting until the buffer runs out
		}
		Entity target = ((EntityHitResult) this.crosshairTarget).getEntity();

		if (cfg.onlyPlayers && !(target instanceof PlayerEntity)) {
			return;
		}
		if (cfg.onlyMidAir && target.isOnGround()) {
			return;
		}
		if (cfg.waitForCooldown && this.player.getAttackCooldownProgress(0.5F) < cfg.cooldownPercent / 100.0F) {
			// Not recharged yet: keep the click alive one more tick (never longer than the buffer).
			return;
		}

		GhostFixClient.bufferLeft = 0;
		this.doAttack();
	}
}
