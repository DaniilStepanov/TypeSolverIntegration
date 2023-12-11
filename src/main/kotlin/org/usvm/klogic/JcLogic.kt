package org.usvm.klogic

import ID
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedAcyclicGraph
import org.jgrapht.traverse.TopologicalOrderIterator
import org.jgs.classtable.ClassesTable
import org.jgs.classtable.extractClassesTable
import org.klogic.core.RelationalContext
import org.klogic.core.Term
import org.klogic.core.and
import utils.JGS.*
import utils.JGS.Closure.Closure
import utils.JtypePretty
import utils.LogicInt

class JcLogic {
    companion object {
        var data: Pair<ClassesTable, DirectedAcyclicGraph<Int, DefaultEdge>> =
            ClassesTable(mutableMapOf()) to DirectedAcyclicGraph(
                DefaultEdge::class.java
            )

        fun initClasses() {
            val ct: ClassesTable = extractClassesTable()
            data = prepareGraph(ct, verbose = false)
        }

        private fun prepareGraph(
            ct: ClassesTable,
            verbose: Boolean = false
        ): Pair<ClassesTable, DirectedAcyclicGraph<Int, DefaultEdge>> {

            val directedGraph: DirectedAcyclicGraph<Int, DefaultEdge> = DirectedAcyclicGraph(
                DefaultEdge::class.java
            )

            fun addEdge(from: Int, to: Class_<LogicInt>) {
                val destID = to.id.asReified().n
                if (from == destID)
                    return
                directedGraph.addVertex(destID)
                if (verbose) {
                    val destName = ct.nameOfId[destID]!!
                    println("Add edge $from  -> ${to.id.asReified()} ($destName)")
                }
                directedGraph.addEdge(from, destID)
            }

            fun addEdge(from: Int, to: Interface<LogicInt>) {
                val destID = to.id.asReified().n
                if (from == destID)
                    return
                directedGraph.addVertex(destID)
                if (verbose) {
                    val destName = ct.nameOfId[destID]!!
                    println("Add edge $from  -> ${to.id.asReified()} ($destName)")
                }
                directedGraph.addEdge(from, to.id.asReified().n)
            }
            for ((id, decl) in ct.table) {
                if (verbose) println("WIP: $id    with `$decl`")
                directedGraph.addVertex(id)
                when (decl) {
                    is C -> when (decl.superClass) {
                        is Class_ -> addEdge(id, decl.superClass as Class_<LogicInt>)
                        is Array_ -> println(
                            "TODO ${Thread.currentThread().stackTrace[2].lineNumber}"
                        )

                        else -> {}
                    }

                    is I -> for (i in decl.supers.asReified().toList()) {
                        when (i) {
                            is Interface -> addEdge(id, i)
                            else -> {}
                        }
                    }
                }
            }
            with(directedGraph) {
                val moreDependencyFirstIterator = TopologicalOrderIterator(
                    directedGraph
                ) // Some class are generated withoout information. Possible Bug
                val toRemove: MutableSet<Int> = mutableSetOf()
                moreDependencyFirstIterator.forEachRemaining { id: Int ->
                    if (ct.table[id] == null) toRemove.add(id)
                }
                toRemove.forEach { directedGraph.removeVertex(it) }
            }
            println("Graph prepareted")
            println(" Total ${ct.table.size} classes")
            println(" Orphaned types: ${ct.missingTypes} ")
            //        println(" nameOfId size =  ${ct.da} ")

            println("Id of 'java.lang.Object' =  ${ct.idOfName["java.lang.Object"]}")
            println("Id of 'java.lang.Iterable' =  ${ct.idOfName["java.lang.Iterable"]}")
            println("Id of 'java.util.List' =  ${ct.idOfName["java.util.List"]}")
            println(" Object with id=1 is ${ct.table[1]}")
            //        println(" Object with id=7671 is ${ct.table[7671]}")
            return (ct to directedGraph)
        }

        private fun <T> Iterable<T>.toCountMap(): Map<out T, Int> = groupingBy { it }.eachCount()

        fun getTypeWithSingleConstraint(
            count: Int = 10,
            boundKind: JGSBackward.ClosureType = JGSBackward.ClosureType.Subtyping,
            bound: (RelationalContext, JGSstandard.ConvenientCT) -> Term<Jtype<ID>>, verbose: Boolean = false
        ) {
            val classTable = JGSstandard.BigCT(data.second, data.first)
            val v = Verifier(classTable)
            val closureBuilder = Closure(classTable)
            with(RelationalContext()) {
                val g = { q: Term<Jtype<ID>> ->
                    and(
                        only_classes_interfaces_and_arrays(q), (when (boundKind) {
                            JGSBackward.ClosureType.Subtyping -> JGSBackward.MakeClosure2(closureBuilder)
                                .closure({ a, b, c, d ->
                                    v.minus_less_minus(a, b, c, d)
                                }, q, bound(this, classTable))

                            JGSBackward.ClosureType.SuperTyping -> JGSBackward.MakeClosure2(closureBuilder)
                                .closure({ a, b, c, d ->
                                    v.minus_less_minus(a, b, c, d)
                                }, bound(this, classTable), q)
                        })
                    )
                }
                val answers = run(count, g).map { it.term }.toList()
                if (verbose) answers.forEachIndexed { i, x -> println("$i: $x") }

//            val expectedTerm = expectedResult(classTable).toCountMap()
                val pp = JtypePretty { classTable.nameOfId(it) }
                val answers2 = answers.map { pp.ppJtype(it) }
                answers2.run {
                    forEachIndexed { i, x -> println("//$i\n\"$x\",") }
                }
//            Assertions.assertEquals(count, answers.count())
//            Assertions.assertEquals(expectedTerm, answers2.toCountMap())
            }
        }

    }


}