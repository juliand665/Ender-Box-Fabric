package enderbox

import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

fun localizationKey(domain: String, identifier: Identifier, path: String) =
	"$domain.${EnderBoxMod.modID}.${identifier.path}.$path"

fun localized(domain: String, identifier: Identifier, path: String, vararg args: Any): Text =
	TranslatableText(localizationKey(domain, identifier, path), *args)
