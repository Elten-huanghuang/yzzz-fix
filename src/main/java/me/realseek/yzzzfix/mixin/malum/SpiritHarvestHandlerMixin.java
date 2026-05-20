package me.realseek.yzzzfix.mixin.malum;

import com.sammy.malum.core.handlers.SpiritHarvestHandler;
import com.sammy.malum.registry.common.SoundRegistry;
import me.realseek.yzzzfix.util.InventoryUtil;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.UUID;

/**
 * 移除 Malum 精魂实体生成，改为直接向玩家（或仆从主人）发放精魂物品。
 *
 * <p>原逻辑通过 {@code createSpiritEntities} 生成追踪实体，
 * 本 Mixin 拦截所有调用，将精魂物品直接放入玩家背包（满时掉落）。
 * 兼容车万女仆等通过 {@link OwnableEntity} / {@link TamableAnimal} 追溯主人的仆从。</p>
 */
@Mixin(value = SpiritHarvestHandler.class, remap = false)
public abstract class SpiritHarvestHandlerMixin {

    @Redirect(method = "spawnItemsAsSpirits",
            at = @At(value = "INVOKE",
                    target = "Lcom/sammy/malum/core/handlers/SpiritHarvestHandler;createSpiritEntities(Lnet/minecraft/world/level/Level;Ljava/util/Collection;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/LivingEntity;)V"),
            remap = false)
    private static void yzzzfix$redirectFromSpawnItems(Level level, Collection<ItemStack> spirits, Vec3 position, LivingEntity attacker) {
        yzzzfix$giveItemsToPlayer(level, spirits, position, attacker);
    }

    @Redirect(method = "shatterItem",
            at = @At(value = "INVOKE",
                    target = "Lcom/sammy/malum/core/handlers/SpiritHarvestHandler;createSpiritEntities(Lnet/minecraft/world/level/Level;Ljava/util/Collection;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/LivingEntity;)V"),
            remap = false)
    private static void yzzzfix$redirectFromShatter(Level level, Collection<ItemStack> spirits, Vec3 position, LivingEntity attacker) {
        yzzzfix$giveItemsToPlayer(level, spirits, position, attacker);
    }

    private static void yzzzfix$giveItemsToPlayer(Level level, Collection<ItemStack> spirits, Vec3 position, LivingEntity attacker) {
        if (level.isClientSide || spirits.isEmpty()) return;

        level.playSound(null, position.x, position.y, position.z,
                SoundRegistry.SOUL_SHATTER.get(), SoundSource.PLAYERS, 1.0F, 0.7F + level.random.nextFloat() * 0.4F);

        Player player = yzzzfix$resolvePlayer(attacker, level);
        for (ItemStack stack : spirits) {
            ItemStack copy = stack.copy();
            if (player != null) {
                InventoryUtil.giveOrDrop(player, copy);
            } else {
                ItemEntity item = new ItemEntity(level, position.x, position.y, position.z, copy);
                item.setPickUpDelay(0);
                level.addFreshEntity(item);
            }
        }
    }

    private static Player yzzzfix$resolvePlayer(LivingEntity attacker, Level level) {
        if (attacker instanceof Player player) return player;
        if (attacker instanceof TamableAnimal tamable && tamable.getOwner() instanceof Player player)
            return player;
        if (attacker instanceof OwnableEntity ownable) {
            UUID uuid = ownable.getOwnerUUID();
            if (uuid != null) {
                return level.getPlayerByUUID(uuid);
            }
        }
        return null;
    }
}