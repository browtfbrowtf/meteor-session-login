package wtf.guzman.rip.mixin;


import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.screens.accounts.AccountsScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wtf.guzman.rip.AddSessionAccountScreen;

@Mixin(value = AccountsScreen.class, remap = false)
public abstract class MixinAccountsScreen extends MixinWidgetScreen {

    @Shadow
    protected abstract void addButton(WContainer c, String text, Runnable action);

    @Inject(method = "initWidgets", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void initWidgets(CallbackInfo ci, WHorizontalList l) {
        addButton(l, "Session", () -> MinecraftClient.getInstance().setScreen(new AddSessionAccountScreen(theme, (AccountsScreen)(Object)this)));
    }
}
