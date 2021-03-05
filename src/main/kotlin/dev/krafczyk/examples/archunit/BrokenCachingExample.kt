package dev.krafczyk.examples.archunit

import io.vavr.control.Try
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.lang.Exception
import java.lang.RuntimeException

@Component
class BrokenCachingExample {
    @Cacheable("cacheA")
    fun returnsTryFailure(arg: String): Try<String> = Try.failure(generateException())

    @Cacheable("cacheB")
    fun throwsExceptions(arg: String): String = throw generateException()

    @Cacheable("cacheB")
    fun returnsNonSerializable(): NonSerializable = NonSerializable("a", "b")

    fun illegallyCallsCacheableMethodOnThis(): String = throwsExceptions("asdf")

    private fun generateException(): Exception = RuntimeException("Error")
}


data class NonSerializable(
    val fieldA: String,
    val fieldB: String
)
