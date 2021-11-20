package com.bennyhuo.kotlin.sample.compiler

import com.tschuchort.compiletesting.kspSourcesDir
import org.junit.Test

/**
 * Created by benny at 2021/6/21 7:00.
 */
class KaptTest {

    fun doTest(path: String) = doTest(path) { KaptCompileUnit(SampleKaptProcessor()) }

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
