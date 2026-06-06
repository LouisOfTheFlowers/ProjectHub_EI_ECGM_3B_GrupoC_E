package com.example.projecthub.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.projecthub.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID

class ObservationPhotoStorageRepository(
    private val contentResolver: ContentResolver,
    private val bucketName: String = "observacao-fotos"
) {

    suspend fun uploadObservationPhoto(
        observationId: Int,
        photoUri: String
    ): Result<String> {
        return try {
            val uri = Uri.parse(photoUri)
            val bytes = contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes()
            } ?: return Result.failure(Exception("Não foi possível abrir a fotografia selecionada."))

            val extension = contentResolver.getType(uri).toFileExtension()
            val contentType = contentResolver.getType(uri).toContentType()
            val path = "observacoes/$observationId/${UUID.randomUUID()}.$extension"
            val bucket = SupabaseClientProvider.client.storage.from(bucketName)

            bucket.upload(path = path, data = bytes) {
                upsert = false
                this.contentType = contentType
            }

            Result.success(bucket.publicUrl(path))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun String?.toFileExtension(): String {
        return when (this?.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }
    }

    private fun String?.toContentType(): ContentType {
        return when (this?.lowercase()) {
            "image/png" -> ContentType.Image.PNG
            "image/webp" -> ContentType("image", "webp")
            "image/gif" -> ContentType.Image.GIF
            else -> ContentType.Image.JPEG
        }
    }
}
