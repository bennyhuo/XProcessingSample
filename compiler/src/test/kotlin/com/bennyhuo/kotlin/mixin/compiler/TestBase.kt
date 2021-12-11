package com.bennyhuo.kotlin.mixin.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.js.inline.util.collectAccessors
import java.io.File
import kotlin.test.assertEquals

/**
 * Created by benny.
 */
const val SOURCE_START_LINE = "// SOURCE"
const val GENERATED_START_LINE = "// GENERATED"
val FILE_NAME_PATTERN = Regex("""// FILE: ((\w+)\.(\w+))\s*""")
val MODULE_NAME_PATTERN = Regex("""// MODULE: ([-\w]+)(\s*/\s*(([-\w]+)(\s*,\s*([-\w]+))*))?""")

const val DEFAULT_MODULE = "default-module"
const val DEFAULT_FILE = "DefaultFile.kt"

class SourceFileInfo(val moduleName: String, val fileName: String, vararg val depends: String) {
    val sourceBuilder = StringBuilder()

    override fun toString(): String {
        return "$fileName: \n$sourceBuilder"
    }

    fun copy(
        module: String = this.moduleName,
        name: String = this.fileName,
        vararg depends: String = this.depends
    ): SourceFileInfo {
        return SourceFileInfo(module, name, *depends)
    }
}

fun parseSourceInfo(lines: List<String>): List<SourceFileInfo> {
    val sourceFileInfos = ArrayList<SourceFileInfo>()
    sourceFileInfos.add(SourceFileInfo(DEFAULT_MODULE, DEFAULT_FILE))
    lines.fold(sourceFileInfos) { acc, line ->
        val moduleResult = MODULE_NAME_PATTERN.find(line)
        if (moduleResult == null) {
            val result = FILE_NAME_PATTERN.find(line)
            if (result == null) {
                acc.last().sourceBuilder.append(line).appendLine()
            } else {
                acc.add(acc.last().copy(name = result.groupValues[1]))
            }
        } else {
            val depends = if (moduleResult.groupValues.size > 4) {
                moduleResult.groupValues[3].split(",")
                    .mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            } else emptyList()
            sourceFileInfos.add(
                SourceFileInfo(
                    moduleResult.groupValues[1],
                    DEFAULT_FILE,
                    *depends.toTypedArray()
                )
            )
        }
        acc
    }
    return sourceFileInfos
}

fun doTest(path: String, creator: (name: String, sources: List<SourceFile>) -> Module) {
    val lines = File(path).readLines()
        .dropWhile { it.trim() != SOURCE_START_LINE }
    val sourceLines =
        lines.takeWhile { it.trim() != GENERATED_START_LINE }.drop(1)
    val generatedLines =
        lines.dropWhile { it.trim() != GENERATED_START_LINE }.drop(1)

    val sourceFileInfos = parseSourceInfo(sourceLines)
    val generatedSourceFileInfos = parseSourceInfo(generatedLines)

    val modules = sourceFileInfos.groupBy {
        it.moduleName
    }.mapValues {
        creator(it.key, it.value.map { sourceFileInfo ->
            SourceFile.new(sourceFileInfo.fileName, sourceFileInfo.sourceBuilder.toString())
        }).also { unit ->
            unit.dependencyNames += it.value.first().depends
        }
    }

    modules.forEach { (_, unit) ->
        unit.resolveDependencies(modules)
    }
    
    
    var left = modules.values
    val argsForMain = mapOf("mixin.main" to "true")
    while (left.isNotEmpty()) {
        left.groupBy { it.isReadyToCompile }
            .let {
                if (it[false] == null) {
                    it[true]?.forEach { it.compile(argsForMain) }    
                } else {
                    it[true]?.forEach { it.compile() }
                }
            }
        
        left = left.filter { !it.isCompiled }
    }
    
    val generatedSourceMap = generatedSourceFileInfos.associateBy { it.moduleName }
    val contentCollector = StringBuilder() to StringBuilder()
    modules.values.forEach { module ->
        contentCollector.first.append("// MODULE: ${module.name}\n")
        contentCollector.second.append("// MODULE: ${module.name}\n")
        module.generatedSourceDir.walkTopDown()
            .filter { !it.isDirectory }
            .forEach {
                contentCollector.first.append("// FILE: ${it.name}\n")
                    .append(generatedSourceMap[module.name]?.sourceBuilder.toString())
                
                contentCollector.second.append("// FILE: ${it.name}\n")
                    .append(it.readText())
            }
        
        assertEquals(module.compileResult?.exitCode, KotlinCompilation.ExitCode.OK)
    }
    
    assertEquals(contentCollector.first, contentCollector.second)
}