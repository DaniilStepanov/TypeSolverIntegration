package org.usvm.klogic

import org.klogic.utils.terms.LogicList
import utils.JGS.Type

class TSUsageExample {

    fun test() {
        try {
            JcLogic.initClasses()
        } catch (e: Throwable) {
            throw e
            //ignore
        }

        JcLogic.getTypeWithSingleConstraint(
            count = 5,
            JGSBackward.ClosureType.Subtyping, { _, ct ->
                val humanName = "java.lang.Iterable"
                val listID = ct.idOfName(humanName)!!
                ct.makeInterface(listID, LogicList.logicListOf(Type(ct.object_t)))
            },
            verbose = false
        )
    }
}