package me.realseek.yzzzfix.mixin.magnet_fix;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.p3pp3rf1y.sophisticatedcore.upgrades.magnet.MagnetUpgradeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(value = MagnetUpgradeWrapper.class, remap = false)
public class MagnetUpgradeWrapperMixin {

    @Redirect(
            method = "pickupItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
                    remap = true
            )
    )
    private <T extends Entity> List<T> yzzzFix$filterThrownItems(
            Level level, EntityTypeTest<Entity, T> entityTypeTest, AABB aabb, Predicate<? super T> predicate
    ) {
        List<T> entities = new ArrayList<>(level.getEntities(entityTypeTest, aabb, predicate));
        entities.removeIf(entity -> {
            if (entity instanceof ItemEntity itemEntity) {
                UUID thrower = ((ItemEntityAccessor) itemEntity).getThrowerUUID();
                return thrower != null;
            }
            return false;
        });
        return entities;
    }
}
