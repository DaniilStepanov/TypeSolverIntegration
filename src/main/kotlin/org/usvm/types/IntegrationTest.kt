package org.usvm.types

import kotlinx.coroutines.runBlocking
import org.jacodb.api.JcClassType
import org.jacodb.api.JcClasspath
import org.jacodb.api.ext.findTypeOrNull
import org.jacodb.impl.features.InMemoryHierarchy
import org.jacodb.impl.jacodb
import java.io.File

class IntegrationTest {
    val fileClassPath = listOf(File("build/libs/TypeSolverIntegration-test.jar"))
    var cp: JcClasspath = initJcdb()
    val typeSolver = TypeSolverImpl(cp)

    init {
        TypeSolverImpl.initClasses(cp)
    }

    private fun initJcdb(): JcClasspath =
        runBlocking {
            val db = jacodb {
                loadByteCode(fileClassPath)
                installFeatures(InMemoryHierarchy)
                useProcessJavaRuntime()
            }
            db.classpath(fileClassPath)
        }

    fun test1() {
        val jcList = cp.findTypeOrNull<List<*>>() as JcClassType
        val randomImplementer = typeSolver.getRandomSubclassOf(listOf(jcList.jcClass))
        val randomConcreteType = typeSolver.getSuitableTypes(jcList.typeParameters.first())
        println("Random implementer of List is ${randomImplementer?.name}")
        println("Random replacement of List type parameter is ${randomConcreteType.first().name}")
    }

    fun test2() {
        val jcGen = cp.findTypeOrNull("org.usvm.example.Gen") as JcClassType
        val randomImplementer = typeSolver.getRandomSubclassOf(listOf(jcGen.jcClass))
        val randomConcreteType = typeSolver.getSuitableTypes(jcGen.typeParameters.first())
        println("Random implementer of List is ${randomImplementer?.name}")
        println("Random replacement of List type parameter is ${randomConcreteType.first().name}")
    }

    fun test3() {
        val jcGeneric = cp.findTypeOrNull("org.usvm.example.Generic") as JcClassType
        val randomConcreteType = typeSolver.getSuitableTypes(jcGeneric.typeParameters.first())
        println("Random replacement of List type parameter is ${randomConcreteType.first().name}")
    }
}