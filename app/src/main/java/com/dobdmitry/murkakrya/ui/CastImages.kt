package com.dobdmitry.murkakrya.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.dobdmitry.murkakrya.R

/**
 * Лица людей — готовые картинки из art/, звери рисуются фигурами.
 *
 * Хотите другое лицо: положите свой файл в art/, запустите
 * `python3 tools/make_faces.py` — фон уберётся сам, картинка попадёт в ресурсы.
 */
val LocalCastImages = staticCompositionLocalOf<Map<Cast, ImageBitmap>> { emptyMap() }

@Composable
fun rememberCastImages(): Map<Cast, ImageBitmap> {
    val kira = ImageBitmap.imageResource(R.drawable.face_kira)
    val mama = ImageBitmap.imageResource(R.drawable.face_mama)
    val papa = ImageBitmap.imageResource(R.drawable.face_papa)
    return remember(kira, mama, papa) {
        mapOf(
            Cast.KIRA to kira,
            Cast.MAMA to mama,
            Cast.PAPA to papa,
        )
    }
}
