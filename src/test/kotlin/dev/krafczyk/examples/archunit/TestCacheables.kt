package dev.krafczyk.examples.archunit

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.Test
import org.springframework.cache.annotation.Cacheable
import java.io.Serializable

class TestCacheables {

    private val notVavrControl = object : DescribedPredicate<JavaClass>("[any value outside of io.vavr.control package]") {
        override fun apply(input: JavaClass): Boolean {
            return !input.packageName.startsWith("io.vavr.control")
        }
    }

    private val implementingSerializable = object : DescribedPredicate<JavaClass>("[any value implementing Serializable]") {
        override fun apply(input: JavaClass): Boolean {
            return input
                .allInterfaces
                .filter { it.name == Serializable::class.java.name }
                .isNotEmpty()
        }
    }

    private val allClasses = ClassFileImporter().importPackages("dev.krafczyk")

    @Test
    fun methodsReturningVavrClassesMayNotBeCached() {
        val rule = ArchRuleDefinition.methods()
            .that().areAnnotatedWith(Cacheable::class.java)
            .should().haveRawReturnType(notVavrControl)
        rule.check(allClasses)
    }

    @Test
    fun cachableMethodsMustReturnSerializables() {
        val rule = ArchRuleDefinition.methods()
            .that().areAnnotatedWith(Cacheable::class.java)
            .should().haveRawReturnType(implementingSerializable)
        rule.check(allClasses)
    }
}
