package me.realseek.yzzzfix.mixin.slashbladeresharped;

import mods.flammpfeil.slashblade.entity.EntityHeavyRainSwords;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 EntityHeavyRainSwords 在客户端因 getOwner() 返回 null 导致崩溃的问题。
 *
 * <p>原版 tick() 方法调用 {@code Objects.requireNonNull(getOwner())}，但在客户端
 * 实体同步阶段 owner 可能尚未到达，导致 NullPointerException。</p>
 *
 * <p>本修复在服务端正常执行完整的 tick 逻辑；在客户端若 owner 为 null 则直接取消
 * 该帧的 tick，避免崩溃，等待下次 owner 同步后再恢复正常行为。</p>
 */
@Mixin(value = EntityHeavyRainSwords.class, remap = false)
public class EntityHeavyRainSwordsMixin {

    @Inject(method = "m_8119_", at = @At("HEAD"), cancellable = true, remap = false)
    private void fixa$skipTickIfOwnerMissing(CallbackInfo ci) {
        EntityHeavyRainSwords self = (EntityHeavyRainSwords) (Object) this;

        if (self.getOwner() == null && self.level().isClientSide()) {
            ci.cancel();
        }
    }
}