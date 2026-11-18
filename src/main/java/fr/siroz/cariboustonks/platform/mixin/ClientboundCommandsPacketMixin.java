package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.siroz.cariboustonks.features.misc.AutoCompleteCommandsFeature;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.network.protocol.game.ClientboundCommandsPacket$NodeResolver")
public abstract class ClientboundCommandsPacketMixin {

	@ModifyExpressionValue(method = "resolve", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket$NodeResolver;resolve(I)Lcom/mojang/brigadier/tree/CommandNode;", ordinal = 1))
	public CommandNode<? extends SharedSuggestionProvider> cariboustonks$AutoCommandSuggestions(CommandNode<SharedSuggestionProvider> original) {
		if (original instanceof LiteralCommandNode<?> literalCommandNode) {
			return switch (literalCommandNode.getLiteral()) {
				case String s when s.equals("warp") && AutoCompleteCommandsFeature.warp != null -> AutoCompleteCommandsFeature.warp;
				case String s when s.equals("gfs") && AutoCompleteCommandsFeature.gfs != null -> AutoCompleteCommandsFeature.gfs;
				case String s when s.equals("bz") && AutoCompleteCommandsFeature.bz != null -> AutoCompleteCommandsFeature.bz;
				case String s when s.equals("call") && AutoCompleteCommandsFeature.call != null -> AutoCompleteCommandsFeature.call;
				default -> original;
			};
		}

		return original;
	}
}
