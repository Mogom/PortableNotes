package com.portablenotes.domain.ui.theme

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.styleStore by preferencesDataStore(name = "app_style")

private object Keys {
    val Color = stringPreferencesKey("color")
    val Fondo = stringPreferencesKey("fondo")
    val Segundo = booleanPreferencesKey("segundo_color")
    val Tarjeta = stringPreferencesKey("tarjeta")
    val Esquinas = stringPreferencesKey("esquinas")
    val Letra = stringPreferencesKey("letra")
    val Barra = stringPreferencesKey("barra")
    val Encabezado = stringPreferencesKey("encabezado")
}

private inline fun <reified E : Enum<E>> Preferences.enum(key: Preferences.Key<String>, default: E): E =
    this[key]?.let { name -> enumValues<E>().firstOrNull { it.name == name } } ?: default

/**
 * Estilo de la app (Temas y Estilo), guardado con DataStore.
 * Se crea a nivel de Activity: el cambio aplica en todas las pantallas al instante.
 */
class StyleViewModel(app: Application) : AndroidViewModel(app) {
    private val store = app.styleStore

    val style: StateFlow<AppStyle> = store.data
        .map { p ->
            val d = AppStyle()
            AppStyle(
                colorId = p[Keys.Color] ?: d.colorId,
                fondo = p.enum(Keys.Fondo, d.fondo),
                segundoColor = p[Keys.Segundo] ?: d.segundoColor,
                tarjeta = p.enum(Keys.Tarjeta, d.tarjeta),
                esquinas = p.enum(Keys.Esquinas, d.esquinas),
                letra = p.enum(Keys.Letra, d.letra),
                barra = p.enum(Keys.Barra, d.barra),
                encabezado = p.enum(Keys.Encabezado, d.encabezado),
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppStyle())

    fun update(transform: (AppStyle) -> AppStyle) {
        val s = transform(style.value)
        viewModelScope.launch {
            store.edit { p ->
                p[Keys.Color] = s.colorId
                p[Keys.Fondo] = s.fondo.name
                p[Keys.Segundo] = s.segundoColor
                p[Keys.Tarjeta] = s.tarjeta.name
                p[Keys.Esquinas] = s.esquinas.name
                p[Keys.Letra] = s.letra.name
                p[Keys.Barra] = s.barra.name
                p[Keys.Encabezado] = s.encabezado.name
            }
        }
    }

    /** "Restablecer estilo": vuelve a Océano, fondo Blanco y el resto por defecto. */
    fun reset() = update { AppStyle() }
}
