package com.martmists.unions

import com.google.auto.service.AutoService
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.Writer

@AutoService(SymbolProcessor::class)
class UnionProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private var done = false
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!done) {
            resolver.getSymbolsWithAnnotation("com.martmists.unions.UnionMethod").forEach(::gather)
            generate()
            done = true
        }

        return emptyList()
    }

    private data class UnionPackage(
        val packageName: String,
        val functions: MutableList<UnionFunction>
    )
    private data class UnionFunction(
        val name: String,
        val items: MutableList<UnionFunctionItem>
    )
    private data class UnionFunctionItem(
        val element: KSAnnotated,
        val annotation: UnionMethod
    )
    private val packages = mutableMapOf<String, UnionPackage>()

    private fun gather(element: KSAnnotated) {
        val packageName = element.containingFile!!.packageName.asString()
        val packageEntry = packages.getOrPut(packageName) { UnionPackage(packageName, mutableListOf()) }
        val annotation = element.getAnnotationsByType(UnionMethod::class).first()
        val function = packageEntry.functions.find { it.name == annotation.name } ?: UnionFunction(annotation.name, mutableListOf()).also { packageEntry.functions.add(it) }
        function.items.add(UnionFunctionItem(element, annotation))
    }

    private fun generate() {
        packages.forEach { (packageName, pkg) ->
            val funcs = pkg.functions
            val file = codeGenerator.createNewFile(
                Dependencies(false, *funcs.flatMap { it.items.map { itt -> itt.element.containingFile!! } }.toTypedArray()),
                "$packageName.union",
                "_unions"
            )
            file.writer().use { w ->
                w.write("@file:Suppress(\"UNCHECKED_CAST\")\npackage $packageName.union\n\n")
                for (function in funcs) {
                    handleFunction(w, function)
                }
            }
        }
    }

    private fun handleFunction(w: Writer, function: UnionFunction) {
        val firstFunc = function.items.maxBy { it.annotation.types.size }
        val firstAnn = firstFunc.annotation
        val firstFn = firstFunc.element as KSFunctionDeclaration

        val genericTypes = mutableMapOf<String, String>()
        val parameters = mutableListOf<String>()

        for ((i, param) in firstAnn.types.withIndex()) {
            val annRaw = firstFn.annotations.first { it.shortName.asString() == "UnionMethod" }
            val annTypes = annRaw.arguments[1].value as List<*>
            val typeArg = (annTypes[i] as KSAnnotation).arguments[0].value as KSType
            val typeName = typeArg.declaration.qualifiedName!!.asString()

            if (param.genericTypeName.isNotEmpty()) {
                genericTypes[param.genericTypeName] = typeName
                parameters.add(param.genericTypeName)
            } else {
                parameters.add(typeName)
            }
        }

        w.write("fun")
        if (genericTypes.isNotEmpty()) {
            w.write(" <${genericTypes.map { (k, v) -> "$k: $v" }.joinToString { it }}>")
        }
        w.write(" ${firstAnn.name}(${firstFn.parameters.map { it.name!!.asString() }.zip(parameters).joinToString { (name, type) -> "$name: $type" }}): ${parameters.last()} {\n")

        for (func in function.items) {
            val fn = func.element as KSFunctionDeclaration
            val checkParams = fn.parameters.filter { it.type.resolve().declaration.qualifiedName!!.asString() !in parameters }
            w.write("""
            |if (${checkParams.joinToString(" && ") { "${it.name!!.asString()} is ${it.type.resolve().declaration.qualifiedName!!.asString()}" }}) {
            |    return ${fn.qualifiedName!!.asString()}(${fn.parameters.joinToString { it.name!!.asString() }}) as ${parameters.last()}
            |}
            """.trimMargin().prependIndent("    ") + "\n")
        }
        w.write("    throw IllegalArgumentException(\"No function ${firstAnn.name} found for given argument types!\")\n}\n")
    }
}
