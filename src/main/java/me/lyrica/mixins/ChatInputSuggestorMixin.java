package me.lyrica.mixins;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lyrica.Lyrica;
import me.lyrica.commands.Command;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Final
    @Shadow
    TextFieldWidget textField;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Final
    @Shadow
    private List<OrderedText> messages;
    @Shadow
    public abstract void show(boolean narrateFirstSuggestion);
    @Unique
    private boolean showOutline = false;

    @Inject(at = {@At(value = "HEAD")}, method = "render")
    private void onRender(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (showOutline) {
            int x = textField.getX() - 3;
            int y = textField.getY() - 3;
            // Outline çizimi (örnek, kendi util fonksiyonunu ekleyebilirsin)
            context.fill(x, y, x + textField.getWidth() + 1, y + 1, 0xFF3399FF); // üst
            context.fill(x, y + textField.getHeight() + 1, x + textField.getWidth() + 1, y + textField.getHeight() + 2, 0xFF3399FF); // alt
            context.fill(x, y, x + 1, y + textField.getHeight() + 2, 0xFF3399FF); // sol
            context.fill(x + textField.getWidth() + 1, y, x + textField.getWidth() + 2, y + textField.getHeight() + 2, 0xFF3399FF); // sağ
        }
    }

    @Inject(at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;getCursor()I", ordinal = 0)}, method = "refresh()V")
    private void onRefresh(CallbackInfo ci) {
        String prefix = Lyrica.COMMAND_MANAGER.getPrefix();
        String string = this.textField.getText();
        showOutline = string.startsWith(prefix);
        if (string.length() > 0) {
            int cursorPos = this.textField.getCursor();
            String string2 = string.substring(0, cursorPos);
            if (prefix.startsWith(string2) || string2.startsWith(prefix)) {
                int j = 0;
                Matcher matcher = Pattern.compile("(\\s+)").matcher(string2);
                while (matcher.find()) {
                    j = matcher.end();
                }
                SuggestionsBuilder builder = new SuggestionsBuilder(string2, j);
                if (string2.length() < prefix.length()) {
                    if (prefix.startsWith(string2)) {
                        builder.suggest(prefix);
                    } else {
                        return;
                    }
                } else if (string2.startsWith(prefix)) {
                    int count = (int) string2.chars().filter(ch -> ch == ' ').count();
                    List<String> seperated = Arrays.asList(string2.split(" "));
                    if (count == 0) {
                        for (Command cmd : Lyrica.COMMAND_MANAGER.getCommands()) {
                            builder.suggest(prefix + cmd.getName() + " ");
                        }
                    } else {
                        if (seperated.size() < 1) return;
                        Command c = Lyrica.COMMAND_MANAGER.getCommand(seperated.get(0).substring(prefix.length()));
                        if (c == null) {
                            messages.add(Text.of("§cno commands found: §e" + seperated.get(0).substring(prefix.length())).asOrderedText());
                            return;
                        }
                        // Eğer Command class'ında autocomplete için fonksiyon yoksa, burada ekleyebilirsin
                        // Örnek: String[] suggestions = c.getAutocorrect(count, seperated);
                        // Şimdilik sadece syntax gösterelim
                        if (!c.getSyntax().isEmpty()) {
                            builder.suggest(c.getSyntax());
                        }
                    }
                } else {
                    return;
                }
                this.pendingSuggestions = builder.buildFuture();
                this.show(false);
            }
        }
    }
} 