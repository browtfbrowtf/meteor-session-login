package wtf.guzman.rip.mixin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = WidgetScreen.class, remap = false)
public class MixinWidgetScreen {

    @Shadow
    @Final
    protected GuiTheme theme;
}
