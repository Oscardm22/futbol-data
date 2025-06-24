package com.example.futboldata.data.repository

import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.model.Partido

interface EquipoRepository {
    /**
     * Obtiene todos los equipos registrados
     * @return Lista de equipos
     */
    suspend fun getEquipos(): List<Equipo>

    /**
     * Obtiene un equipo específico por su ID
     * @param equipoId ID del equipo a buscar
     * @return Equipo encontrado
     * @throws Exception si el equipo no existe
     */
    suspend fun getEquipoById(equipoId: String): Equipo

    /**
     * Guarda un nuevo equipo o actualiza uno existente
     * @param equipo Datos del equipo a guardar
     * @return ID del equipo guardado
     */
    suspend fun saveEquipo(equipo: Equipo): String

    /**
     * Elimina un equipo existente
     * @param equipoId ID del equipo a eliminar
     */
    suspend fun deleteEquipo(equipoId: String)

    /**
     * Obtiene todos los partidos de un equipo específico
     * @param equipoId ID del equipo
     * @return Lista de partidos del equipo
     */
    suspend fun getPartidos(equipoId: String): List<Partido>

    /**
     * Obtiene un equipo junto con sus estadísticas calculadas
     * @param equipoId ID del equipo
     * @return Par que contiene el equipo y sus estadísticas
     */
    suspend fun getEquipoWithStats(equipoId: String): Pair<Equipo, Estadisticas>

    /**
     * Obtiene los últimos partidos de un equipo
     * @param equipoId ID del equipo
     * @param limit Número máximo de partidos a obtener
     * @return Lista de partidos ordenados por fecha descendente
     */
    suspend fun getUltimosPartidos(equipoId: String, limit: Int): List<Partido>
}