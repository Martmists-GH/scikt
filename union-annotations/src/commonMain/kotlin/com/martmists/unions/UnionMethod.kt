package com.martmists.unions

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
annotation class Type(
    val baseType: KClass<*>,
    val genericTypeName: String = "",
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class UnionMethod(val name: String, vararg val types: Type)
