package me.realseek.yzzzfix.mixin.magnet_fix;

import com.cozary.nameless_trinkets.items.trinkets.BrokenMagnet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(value = BrokenMagnet.class, remap = false)
public class BrokenMagnetMixin {

    @Redirect(
            method = "curioTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
                    remap = true
            )
    )
    private <T extends Entity> List<T> yzzzFix$filterThrownItems(
            Level level, Class<T> entityClass, AABB aabb
    ) {
        List<T> entities = new ArrayList<>(level.getEntitiesOfClass(entityClass, aabb));
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
