package org.usvm.types

import org.usvm.klogic.TSUsageExample

fun main() {
    val it = IntegrationTest()
    it.test1()
    println("------------------")
    it.test2()
    println("------------------")
    it.test3()
    println("-------")
    val tes = TSUsageExample()
    tes.test()
}