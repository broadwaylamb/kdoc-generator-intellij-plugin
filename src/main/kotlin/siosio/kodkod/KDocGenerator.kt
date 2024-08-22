package siosio.kodkod

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaNamedSymbol
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

interface KDocGenerator {
    fun generate(): String
}

private fun toParamsKdoc(keyword: String = "@param", params: List<KaNamedSymbol>): String =
    params.map { "$keyword ${it.name}" }
        .joinToString("\n", transform = { "* $it" })

private val KaDeclarationSymbol.safeTypeParameters: List<KaTypeParameterSymbol>
    get() = try {
        @OptIn(KaExperimentalApi::class)
        typeParameters
    } catch (_: LinkageError) {
        emptyList()
    }

class NamedFunctionKDocGenerator(private val function: KtNamedFunction) : KDocGenerator {
    override fun generate(): String {
        val builder = StringBuilder()
        builder.appendLine("/**")
                .appendLine("* TODO")
                .appendLine("*")
        analyze(function) {
            val symbol = function.symbol

            val typeParameters = symbol.safeTypeParameters
            if (typeParameters.isNotEmpty()) {
                builder.appendLine(toParamsKdoc(params = typeParameters))
            }

            if (symbol.valueParameters.isNotEmpty()) {
                builder.appendLine(toParamsKdoc(params = symbol.valueParameters))
            }

            if (!symbol.returnType.isUnitType) {
                builder.appendLine("* @return")
            }
        }
        builder.appendLine("*/")
        return builder.toString()
    }
}

class ClassKDocGenerator(private val klass: KtClass) : KDocGenerator {
    override fun generate(): String {
        val builder = StringBuilder()
        builder.appendLine("/**")
                .appendLine("* TODO")
                .appendLine("*")

        analyze(klass) {
            val symbol = klass.classSymbol ?: return@analyze
            val typeParameters = symbol.safeTypeParameters
            if (typeParameters.isNotEmpty()) {
                builder.appendLine(toParamsKdoc(params = typeParameters))
            }

            val declaredMemberScope = symbol.declaredMemberScope
            val properties = declaredMemberScope.callables
                .filterIsInstance<KaPropertySymbol>()
                .filter { it.isFromPrimaryConstructor }
                .toList()
            val parameters = declaredMemberScope.constructors
                .singleOrNull { it.isPrimary }
                ?.valueParameters
                .orEmpty()
                .filter { properties.none { p -> p.name == it.name} }

            if (properties.isNotEmpty()) {
                builder.appendLine(toParamsKdoc(keyword = "@property", params = properties))
            }

            if (parameters.isNotEmpty()) {
                builder.appendLine("* @constructor")
                    .appendLine("* TODO")
                    .appendLine("*")
                    .appendLine(toParamsKdoc(params = parameters))
            }
        }
        
        builder.appendLine("*/")
        return builder.toString()
    }
}
