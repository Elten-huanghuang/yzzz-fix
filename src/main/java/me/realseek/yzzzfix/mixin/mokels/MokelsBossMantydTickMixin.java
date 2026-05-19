package me.realseek.yzzzfix.mixin.mokels;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复 Mokel's Boss Mantyd 延迟任务在执行时因目标实体消失导致的空指针崩溃。
 *
 * <p>{@code BossMantydAttackPatternProcedure.execute} 通过 MCreator 的
 * {@code queueServerWork} 注册延迟 lambda 到任务队列。当 lambda 在后续 tick
 * 执行时，Boss 的攻击目标可能已经死亡或离开，导致 {@code getTarget()} 返回 null
 * 并在调用 {@code getY()} 等方法时抛出 NPE。</p>
 *
 * <p>本 Mixin 重定向 {@code lambda$tick$2} 中的 {@code Runnable.run()} 调用，
 * 仅在延迟任务实际执行时捕获 NPE，避免影响其他 forEach 逻辑。</p>
 */
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "net.mcreator.mokelsbossmantyd.MokelsBossMantydMod", remap = false)
public abstract class MokelsBossMantydTickMixin {

    @Unique
    private static final Logger yzzzfix$LOGGER = LogManager.getLogger("YzzzFix");

    @Unique
    private static boolean yzzzfix$loggedOnce = false;

    @Redirect(
            method = "lambda$tick$2",
            at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V")
    )
    private static void yzzzfix$safeRunDelayedTask(Runnable runnable) {
        try {
            runnable.run();
        } catch (NullPointerException e) {
            if (!yzzzfix$loggedOnce) {
                yzzzfix$loggedOnce = true;
                yzzzfix$LOGGER.warn("Suppressed NPE from MokelsBossMantyd delayed task (further occurrences hidden)", e);
            }
        }
    }
}
