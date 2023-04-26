package moe.nea.sky.platform.fabric.mixin;

import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CommandNode.class)
public interface AccessorCommandNode<S> {
    @Accessor("children")
    Map<String, CommandNode<S>> getChildren_skyneamoe();
}
