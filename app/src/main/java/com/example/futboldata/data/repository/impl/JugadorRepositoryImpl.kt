package com.example.futboldata.data.repository.impl

import android.util.Log
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.repository.JugadorRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class JugadorRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : JugadorRepository {

    override suspend fun addJugador(jugador: Jugador): String {
        val documentRef = db.collection("jugadores").document()
        val jugadorConId = jugador.copy(id = documentRef.id)

        documentRef.set(jugadorConId.toFirestoreMap()).await()
        return documentRef.id
    }

    override suspend fun getJugadoresPorEquipo(equipoId: String): List<Jugador> {
        Log.d("DEBUG_REPO", "▶ [Firestore] Consultando jugadores para equipoId: $equipoId")
        return try {
            val querySnapshot = db.collection("jugadores")
                .whereEqualTo("equipoId", equipoId)
                .get()
                .await()

            Log.d("DEBUG_REPO", "✓ [Firestore] Documentos encontrados: ${querySnapshot.documents.size}")
            querySnapshot.documents.mapNotNull { document ->
                val golesMap = when (val goles = document.get("golesPorCompeticion")) {
                    is Map<*, *> -> goles.mapNotNull { (key, value) ->
                        if (key is String && value is Int) key to value else null
                    }.toMap()
                    else -> null
                }

                Jugador.fromFirestore(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    posicion = document.getString("posicion") ?: "PO",
                    equipoId = document.getString("equipoId") ?: "",
                    goles = golesMap
                )
            }
        } catch (e: Exception) {
            Log.e("DEBUG_REPO", "✕ [Firestore] Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun eliminarJugador(jugadorId: String) {
        try {
            Log.d("DEBUG_REPO", "▶ [Firestore] Eliminando jugador con ID: $jugadorId")
            db.collection("jugadores").document(jugadorId).delete().await()
            Log.d("DEBUG_REPO", "✓ [Firestore] Jugador eliminado correctamente")
        } catch (e: Exception) {
            Log.e("DEBUG_REPO", "✕ [Firestore] Error al eliminar jugador: ${e.message}")
            throw Exception("No se pudo eliminar el jugador: ${e.message}")
        }
    }
}