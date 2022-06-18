package ru.vtb.mssa.digi.integration.migr.exception

class InvalidFieldException(override val message: String?) : BaseException(
     "Invalid field format - $message"
)