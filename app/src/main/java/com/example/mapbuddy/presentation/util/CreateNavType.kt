package com.example.mapbuddy.presentation.util

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

inline fun <reified T : Parcelable> createNavType(
    isNullableAllowed: Boolean = false,
    json: Json = Json
): NavType<T> {
    return object : NavType<T>(isNullableAllowed) {
        override fun get(bundle: Bundle, key: String): T? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(key, T::class.java)
            } else {
                @Suppress("DEPRECATED")
                bundle.getParcelable(key)
            }
        }

        override fun parseValue(value: String): T {
            return json.decodeFromString(value)
        }


        override fun put(bundle: Bundle, key: String, value: T) {
            bundle.putParcelable(key, value)
        }

        override fun serializeAsValue(value: T): String {
            return json.encodeToString(value)
        }
    }
}

inline fun <reified T : Parcelable> createTypeMap(
    isNullableAllowed: Boolean = false,
    json: Json = Json
): Map<KType, NavType<T>> {
    return mapOf(typeOf<T>() to createNavType<T>(isNullableAllowed, json))
}

