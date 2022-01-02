package com.bennyhuo.kotlin.mixin.compiler

import com.tschuchort.compiletesting.SourceFile
import org.junit.Test

/**
 * Created by benny at 2021/6/21 7:00.
 */
class KspTest {

    fun doTest(path: String) = doTest(path) { moduleInfo ->
        KspModule(
            moduleInfo.name,
            moduleInfo.args,
            moduleInfo.sourceFileInfos.map { sourceFileInfo ->
                SourceFile.new(sourceFileInfo.fileName, sourceFileInfo.sourceBuilder.toString())
            },
            moduleInfo.dependencies,
            MixinKspProcessor.Provider()
        )
    }

    @Test
    fun testBasic() {
        doTest("testData/Basic.kt")
    }

    @Test
    fun testGeneric() {
        doTest("testData/Generics.kt")
    }

    @Test
    fun testAnnotations() {
        doTest("testData/Annotations.kt")
    }

    @Test
    fun testModules() {
        doTest("testData/Modules.kt")
    }
}