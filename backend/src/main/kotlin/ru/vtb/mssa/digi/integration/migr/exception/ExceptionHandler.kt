package ru.vtb.mssa.digi.integration.migr.exception

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@RestControllerAdvice
class ExceptionHandler {
    companion object {
        private const val CAUGHT_MESSAGE = "\nExceptionHandler : Caught exception:"
        private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        request: HttpServletRequest, exception: ConstraintViolationException,
    ) {
        log.error(CAUGHT_MESSAGE, exception)
        val constraintViolation = exception.constraintViolations?.stream()?.findAny()?.orElse(null)
        val annotationClass = constraintViolation?.constraintDescriptor?.annotation?.annotationClass
        if (NotNull::class == annotationClass || NotEmpty::class == annotationClass || NotBlank::class == annotationClass) {
            throw InvalidFieldException(constraintViolation.propertyPath?.last()?.toString())
        } else {
            throw InvalidFieldException(constraintViolation?.propertyPath?.last()?.toString())
        }
    }

    @ExceptionHandler(MissingPathVariableException::class)
    fun handleMissingPathVariableException(
        request: HttpServletRequest, exception: MissingPathVariableException,
    ) {
        log.error(CAUGHT_MESSAGE, exception)
        throw InvalidFieldException(exception.variableName)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        request: HttpServletRequest, exception: MissingServletRequestParameterException,
    ) {
        log.error(CAUGHT_MESSAGE, exception)
        throw InvalidFieldException(exception.parameterName)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException,
    ) {
        log.error(CAUGHT_MESSAGE, exception)
        exception.fieldError?.let {
            if (it.rejectedValue == null) {
                throw InvalidFieldException(it.field)
            } else {
                throw InvalidFieldException(it.field)
            }
        }
        throw ExternalServiceUnavailableException(exception.message)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        request: HttpServletRequest, exception: MethodArgumentTypeMismatchException,
    ) {
        log.error(CAUGHT_MESSAGE, exception)
        throw InvalidFieldException(exception.name)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        request: HttpServletRequest, exception: HttpMessageNotReadableException,
    ) {
        log.error(CAUGHT_MESSAGE, exception)
        throw when (val cause = exception.cause) {
            is JsonProcessingException -> InvalidFieldException(cause.toString())
            else -> {
                throw InvalidFieldException(exception.message)
            }
        }
    }

}