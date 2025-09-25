package fr.siroz.cariboustonks.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import fr.siroz.cariboustonks.util.StonksUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = YggdrasilServicesKeyInfo.class, remap = false)
public abstract class YggdrasilServicesKeyInfoMixin {

	@WrapWithCondition(method = "validateProperty", remap = false, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private boolean cariboustonks$stopLogMalformedSignatureEncoding(Logger logger, String message, Object property, Object exception) {
		return !StonksUtils.isConnectedToHypixel();
	}
}
