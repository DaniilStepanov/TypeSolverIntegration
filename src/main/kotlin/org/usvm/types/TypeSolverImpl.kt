package org.usvm.types

import org.jacodb.api.JcClassOrInterface
import org.jacodb.api.JcClasspath
import org.jacodb.api.JcTypeVariableDeclaration
import org.jacodb.api.ext.allSuperHierarchySequence
import org.jacodb.api.ext.objectClass
class TypeSolverImpl(
    val jcClasspath: JcClasspath
) : TypeSolver {

    companion object {
        lateinit var classes: List<JcClassOrInterface>

        fun initClasses(jcClasspath: JcClasspath) {
            classes = jcClasspath.locations
                .flatMap { it.classNames ?: setOf() }
                .filter { it.contains("java.") || it.contains("example") }
                .mapNotNull { jcClasspath.findClassOrNull(it) }
        }
    }

    /**
     * Simple algorithm when we calc type only suitable for single upper bound
     */
    override fun getSuitableTypes(type: JcTypeVariableDeclaration): Sequence<JcClassOrInterface> {
        val upperBound = type.bounds.firstOrNull()?.jcClass ?: jcClasspath.objectClass
        return classes.asSequence()
            .shuffled()
            .filter { it.allSuperHierarchySequence.contains(upperBound) }
    }

    override fun getRandomSubclassOf(superClasses: List<JcClassOrInterface>): JcClassOrInterface? =
        classes.shuffled().firstOrNull { jcClass ->
            if (jcClass.isInterface || jcClass.isAbstract) {
                return@firstOrNull false
            }
            if (jcClass.outerClass != null && !jcClass.isStatic) return@firstOrNull false
            try {
                superClasses.all { superClass -> jcClass.allSuperHierarchySequence.contains(superClass) }
            } catch (e: Throwable) {
                false
            }
        }
}