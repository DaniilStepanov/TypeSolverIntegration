package org.usvm.types

import JGSstandard
import JGSstandard.Companion.prepareGraph
import JGSstandard.Companion.solverOfBigCT
import JGSstandard.Companion.toJCDBType
import kotlinx.coroutines.runBlocking
import org.jacodb.api.JcClassType
import org.jacodb.api.JcClasspath
import org.jacodb.api.ext.findTypeOrNull
import org.jacodb.impl.features.InMemoryHierarchy
import org.jacodb.impl.jacodb
import org.jgs.classtable.ClassesTable
import org.jgs.classtable.extractClassesTable
import java.io.File
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedAcyclicGraph
import org.jgrapht.traverse.TopologicalOrderIterator

class IntegrationTest {
    val fileClassPath = listOf(File("build/libs/TypeSolverIntegration-test.jar"))
    var cp: JcClasspath = initJcdb()
    lateinit var  typeSolver : org.jgs.classtable.TypeSolver

    init {
        val ct: ClassesTable = extractClassesTable()
        val data : Pair<ClassesTable, DirectedAcyclicGraph<Int, DefaultEdge>> =
            prepareGraph(ct, verbose = false)
        val classTable = JGSstandard.BigCT(data.second, data.first)
        val classpath = data.first.classPath!!
        typeSolver = solverOfBigCT(classTable, classpath, ::toJCDBType)
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
        println("Random implementer of List is ${randomImplementer?.name}")
        val randomConcreteType = typeSolver.getSuitableTypes(jcList.typeParameters.first())
        println("Random replacement of List type parameter is ${randomConcreteType?.name}")
    }
    
    fun test2() {
        // Not(Kakadu): I dont' have these classes
        /*
        val jcGen = cp.findTypeOrNull("org.usvm.example.Gen") as JcClassType
        val randomImplementer = typeSolver.getRandomSubclassOf(listOf(jcGen.jcClass))
        println("Random implementer of List is ${randomImplementer?.name}")
        val randomConcreteType = typeSolver.getSuitableTypes(jcGen.typeParameters.first())
        println("Random replacement of List type parameter is ${randomConcreteType?.name}")

         */
    }

    fun test3() {
        // Not(Kakadu): I dont' have these classes
        /*
        val jcGeneric = cp.findTypeOrNull("org.usvm.example.Generic") as JcClassType
        val randomConcreteType = typeSolver.getSuitableTypes(jcGeneric.typeParameters.first())
        println("Random replacement of List type parameter is ${randomConcreteType?.name}")

         */
    }


}
