/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.units.plugin.generator

import com.squareup.kotlinpoet.*
import java.io.File

open class UnitItemGenerator(
    val className: String,
    val classPackage: String,
    val dataBindingPackage: String,
    val layoutName: String,
    val variables: Map<String, String>,
    val imports: List<String>
) {
    open fun generate(generationDir: File) {
        val unitClass = TypeSpec.classBuilder(className)

        unitClass.addModifiers(KModifier.OPEN)
        unitClass.addSuperinterface(ClassName.bestGuess("dev.icerock.moko.units.UnitItem"))
        unitClass.addProperty(
            PropertySpec.builder("itemId", Long::class, KModifier.OVERRIDE)
                .mutable(true)
                .initializer("RecyclerView.NO_ID")
                .build()
        )
        unitClass.addProperty(
            PropertySpec.builder("layoutId", Int::class, KModifier.OVERRIDE)
                .initializer("R.layout.$layoutName")
                .build()
        )

        variables.toSortedMap().forEach { (variableKey, variableType) ->
            val propertyName = getVariablePropertyName(variableKey)

            val className = variableType.takeIf { it.contains(".") }
                ?: imports.firstOrNull { it.endsWith(".$variableType") }
                ?: "kotlin.${variableType.capitalize()}"

            val classType = ClassName.bestGuess(className).copy(nullable = true)

            val property = PropertySpec.builder(
                propertyName,
                classType
            ).initializer("null")
                .mutable(true)
                .build()
            unitClass.addProperty(property)
        }

        unitClass.addFunction(
            FunSpec.builder("bind")
                .addParameter(
                    ParameterSpec.builder(
                        "viewDataBinding",
                        ClassName.bestGuess("androidx.databinding.ViewDataBinding")
                    ).build()
                )
                .addModifiers(KModifier.OVERRIDE)
                .addCode(buildBindCode())
                .build()
        )

        val typeSpec = unitClass.build()

        val fileSpec = FileSpec.get(
            packageName = classPackage,
            typeSpec = typeSpec
        ).toBuilder().apply {
            imports.forEach { import ->
                val type = ClassName.bestGuess(import)
                addImport(type.packageName, type.simpleName)
            }
            addImport(dataBindingPackage, "BR")
            addImport("androidx.recyclerview.widget", "RecyclerView")
        }.build()

        fileSpec.writeTo(generationDir)
    }

    protected fun buildBindCode(): CodeBlock {
        return CodeBlock.builder()
            .apply {
                variables.keys.sorted().forEach { key ->
                    val bindingKey = getVariableBindingKey(key)
                    val propertyName = getVariablePropertyName(key)
                    addStatement("viewDataBinding.setVariable($bindingKey, $propertyName)")
                }
            }.build()
    }

    private fun getVariablePropertyName(variableKey: String): String {
        return variableKey
    }

    private fun getVariableBindingKey(variableKey: String): String {
        return "BR.$variableKey"
    }
}