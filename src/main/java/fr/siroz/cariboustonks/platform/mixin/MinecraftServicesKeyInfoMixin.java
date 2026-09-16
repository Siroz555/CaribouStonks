package fr.siroz.cariboustonks.platform.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.services.MinecraftServicesKeyInfo;
import fr.siroz.cariboustonks.util.StonksUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServicesKeyInfo.class)
public abstract class MinecraftServicesKeyInfoMixin {

	@WrapWithCondition(method = "validateProperty", remap = false, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private boolean cariboustonks$stopLogMalformedSignatureEncoding(Logger logger, String message, Object property, Object exception) {
		return !StonksUtils.isConnectedToHypixel();
	}
}
