package org.usvm.types

import org.jacodb.api.JcClassOrInterface
import org.jacodb.api.JcTypeVariableDeclaration

interface TypeSolver {
    fun getSuitableTypes(type: JcTypeVariableDeclaration): Sequence<JcClassOrInterface>
    fun getRandomSubclassOf(superClasses: List<JcClassOrInterface>): JcClassOrInterface?
}