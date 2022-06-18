package ru.vtb.mssa.digi.integration.migr.exception

open class BaseException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : RuntimeException()