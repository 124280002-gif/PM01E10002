package com.example.pm01e10002.models

data class Contacto(
    val id: Int = 0,
    val pais: String,
    val nombre: String,
    val telefono: String,
    val nota: String,
    val foto: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contacto

        if (id != other.id) return false
        if (pais != other.pais) return false
        if (nombre != other.nombre) return false
        if (telefono != other.telefono) return false
        if (nota != other.nota) return false
        if (foto != null) {
            if (other.foto == null) return false
            if (!foto.contentEquals(other.foto)) return false
        } else if (other.foto != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + pais.hashCode()
        result = 31 * result + nombre.hashCode()
        result = 31 * result + telefono.hashCode()
        result = 31 * result + nota.hashCode()
        result = 31 * result + (foto?.contentHashCode() ?: 0)
        return result
    }
}
