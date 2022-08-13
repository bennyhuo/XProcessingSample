package com.bennyhuo.kotlin.processor.module

import com.bennyhuo.kotlin.processor.module.utils.generateName
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.TypeSpec

/**
 * Created by benny.
 */

interface IndexGenerator<Element> {

    fun generate(elements: Collection<Element>)

}

interface IndexGeneratorForJava<Element> : IndexGenerator<Element> {

    fun getElementName(element: Element): String

    fun addOriginatingElements(typeSpecBuilder: TypeSpec.Builder, elements: Collection<Element>)

    fun writeType(typeSpec: TypeSpec)

    override fun generate(elements: Collection<Element>) {
        if (elements.isEmpty()) return

        val sortedElementNames = elements.map { getElementName(it) }.distinct().sortedBy { it }

        val indexName = "LibraryIndex_${generateName(sortedElementNames)}"
        val typeSpec = TypeSpec.classBuilder(indexName)
            .addAnnotation(
                AnnotationSpec.builder(LibraryIndex::class.java)
                    .addMember(
                        "value", "{${sortedElementNames.joinToString { "\$S" }}}",
                        *sortedElementNames.toTypedArray()
                    ).build()
            ).also { typeBuilder ->
                addOriginatingElements(typeBuilder, elements)
            }
            .build()

        writeType(typeSpec)
    }

}