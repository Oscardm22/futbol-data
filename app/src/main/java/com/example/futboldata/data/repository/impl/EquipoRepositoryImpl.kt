package com.example.futboldata.data.repository.impl

import android.util.Log
import com.example.futboldata.data.repository.EquipoRepository
import com.example.futboldata.data.model.*
import com.example.futboldata.utils.StatsCalculator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class EquipoRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val statsCalculator: StatsCalculator
) : EquipoRepository {

    // --- Equipos ---
    override suspend fun getEquipos(): List<Equipo> {
        return try {
            db.collection("equipos")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    // Primero obtener el objeto
                    val equipo = document.toObject(Equipo::class.java)
                    // Luego asegurarnos que tenga el ID correcto
                    equipo?.copy(id = document.id).also {
                        Log.d("FIREBASE_DEBUG", "Equipo cargado: ${it?.nombre} - ID: ${it?.id}")
                    }
                }
        } catch (e: Exception) {
            Log.e("EquipoRepository", "Error al obtener equipos", e)
            emptyList<Equipo>().also {
                Log.e("FIREBASE_DEBUG", "Error cargando equipos: ${e.message}")
            }
        }
    }

    override suspend fun getEquipoById(equipoId: String): Equipo? {
        return try {
            db.collection("equipos")
                .document(equipoId)
                .get()
                .await()
                .let { document ->
                    if (document.exists()) {
                        document.toObject(Equipo::class.java)?.copy(id = document.id)
                    } else {
                        null
                    }
                }
        } catch (e: Exception) {
            Log.e("EquipoRepository", "Error al obtener equipo", e)
            null
        }
    }

    override suspend fun saveEquipo(equipo: Equipo): String {
        return try {
            if (equipo.id.isEmpty()) {
                // 1. Creamos una referencia de documento con ID generado
                val docRef = db.collection("equipos").document()
                // 2. Creamos una copia del equipo con el ID asignado
                val equipoConId = equipo.copy(id = docRef.id)
                // 3. Guardamos el equipo completo (con ID)
                docRef.set(equipoConId).await()
                // 4. Retornamos el ID generado
                docRef.id
            } else {
                // Caso de actualización (sin cambios)
                db.collection("equipos")
                    .document(equipo.id)
                    .set(equipo)
                    .await()
                equipo.id
            }
        } catch (e: Exception) {
            Log.e("EquipoRepository", "Error al guardar equipo", e)
            ""
        }
    }

    override suspend fun deleteEquipo(equipoId: String) {
        try {
            db.collection("equipos")
                .document(equipoId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("EquipoRepository", "Error al eliminar equipo", e)
            throw e
        }
    }

    // --- Competiciones (sin cambios) ---
    override suspend fun getCompeticiones(): List<Competicion> {
        return db.collection("competiciones")
            .get()
            .await()
            .toObjects(Competicion::class.java)
    }

    // --- Estadísticas ---
    override suspend fun getEquipoWithStats(equipoId: String, partidos: List<Partido>): Pair<Equipo?, Estadisticas?> {
        return try {
            val equipo = getEquipoById(equipoId)
            val stats = statsCalculator.calculate(partidos)
            Pair(equipo, stats)
        } catch (e: Exception) {
            Log.e("RepoError", "Error al procesar estadísticas:", e)
            Pair(null, null)
        }
    }
}