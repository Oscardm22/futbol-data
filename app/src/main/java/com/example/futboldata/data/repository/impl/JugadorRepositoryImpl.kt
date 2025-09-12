package com.example.futboldata.data.repository.impl

import android.util.Log
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.toFirestoreMap
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
                .whereEqualTo("activo", true)
                .get()
                .await()

            Log.d("DEBUG_REPO", "✓ [Firestore] Documentos encontrados: ${querySnapshot.documents.size}")
            querySnapshot.documents.mapNotNull { document ->
                // Crear mapa con los datos del documento
                val data = hashMapOf<String, Any>().apply {
                    put("nombre", document.getString("nombre") ?: "")
                    put("posicion", document.getString("posicion") ?: "PO")
                    put("equipoId", document.getString("equipoId") ?: "")
                    put("activo", document.getBoolean("activo") != false)

                    // Manejar campos numéricos
                    document.getLong("partidosJugados")?.let { put("partidosJugados", it) }
                    document.getLong("goles")?.let { put("goles", it) }
                    document.getLong("asistencias")?.let { put("asistencias", it) }
                    document.getLong("porteriasImbatidas")?.let { put("porteriasImbatidas", it) }
                    document.getLong("mvp")?.let { put("mvp", it) }

                    // Manejar mapas de competiciones
                    document.get("partidosPorCompeticion")?.let { put("partidosPorCompeticion", it) }
                    document.get("golesPorCompeticion")?.let { put("golesPorCompeticion", it) }
                    document.get("asistenciasPorCompeticion")?.let { put("asistenciasPorCompeticion", it) }
                    document.get("porteriasImbatidasPorCompeticion")?.let { put("porteriasImbatidasPorCompeticion", it) }
                    document.get("mvpPorCompeticion")?.let { put("mvpPorCompeticion", it) }
                }

                Jugador.fromFirestore(
                    id = document.id,
                    data = data
                )
            }
        } catch (e: Exception) {
            Log.e("DEBUG_REPO", "✕ [Firestore] Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun eliminarJugador(jugadorId: String) {
        try {
            Log.d("DEBUG_REPO", "▶ [Firestore] Desactivando jugador con ID: $jugadorId")
            db.collection("jugadores").document(jugadorId)
                .update("activo", false)
                .await()
            Log.d("DEBUG_REPO", "✓ [Firestore] Jugador desactivado correctamente")
        } catch (e: Exception) {
            Log.e("DEBUG_REPO", "✕ [Firestore] Error al desactivar jugador: ${e.message}")
            throw Exception("No se pudo desactivar el jugador: ${e.message}")
        }
    }

    override suspend fun updateJugador(jugador: Jugador) {
        try {
            db.collection("jugadores")
                .document(jugador.id)
                .set(jugador.toFirestoreMap())
                .await()
            Log.d("DEBUG_REPO", "✓ [Firestore] Jugador ${jugador.id} actualizado")
        } catch (e: Exception) {
            Log.e("DEBUG_REPO", "✕ [Firestore] Error al actualizar jugador: ${e.message}")
            throw Exception("No se pudo actualizar el jugador: ${e.message}")
        }
    }
}