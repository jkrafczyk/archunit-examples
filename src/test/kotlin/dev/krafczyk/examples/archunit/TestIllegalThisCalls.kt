package dev.krafczyk.examples.archunit

import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.Test
import org.springframework.cache.annotation.Cacheable

class TestIllegalThisCalls {
    val notBeCalledFromThis = object : ArchCondition<JavaMethod>("not be called via the this object") {
        override fun check(method: JavaMethod, events: ConditionEvents) {
            method.callsOfSelf
                .filter { it.originOwner == it.targetOwner }
                .forEach { events.add(SimpleConditionEvent(it.originOwner, false, "Method <${method.fullName}> illegally called directly on this by <${it.origin.fullName}>")) }
        }
    }

    private val allClasses = ClassFileImporter().importPackages("dev.krafczyk")

    @Test
    fun cacheableMethodsShouldNotBeCalledOnThis() {
        val rule = ArchRuleDefinition.methods()
            .that().areAnnotatedWith(Cacheable::class.java)
            .should(notBeCalledFromThis)
        rule.check(allClasses)
    }
}
