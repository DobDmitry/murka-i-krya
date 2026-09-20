package com.dobdmitry.murkakrya.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.dobdmitry.murkakrya.R

/**
 * Лица героев — готовые картинки.
 *
 * Хотите другое лицо: положите свой файл в art/, впишите его в
 * tools/make_faces.py и запустите скрипт — фон уберётся сам,
 * картинка попадёт в ресурсы.
 */
val LocalCastImages = staticCompositionLocalOf<Map<Cast, ImageBitmap>> { emptyMap() }

@Composable
fun rememberCastImages(): Map<Cast, ImageBitmap> {
    val kira = ImageBitmap.imageResource(R.drawable.face_kira)
    val mama = ImageBitmap.imageResource(R.drawable.face_mama)
    val papa = ImageBitmap.imageResource(R.drawable.face_papa)
    val artem = ImageBitmap.imageResource(R.drawable.face_artem)
    val babaIra = ImageBitmap.imageResource(R.drawable.face_baba_ira)
    val roma = ImageBitmap.imageResource(R.drawable.face_roma)
    val babaLuba = ImageBitmap.imageResource(R.drawable.face_baba_luba)
    val habib = ImageBitmap.imageResource(R.drawable.face_habib)
    val dedaMisha = ImageBitmap.imageResource(R.drawable.face_deda_misha)
    val uchitel = ImageBitmap.imageResource(R.drawable.face_uchitel)
    val matvey = ImageBitmap.imageResource(R.drawable.face_matvey)
    val ilya = ImageBitmap.imageResource(R.drawable.face_ilya)

    return remember(kira, mama, papa, artem, babaIra, roma, babaLuba, habib, dedaMisha, uchitel, matvey, ilya) {
        mapOf(
            Cast.KIRA to kira,
            Cast.MAMA to mama,
            Cast.PAPA to papa,
            Cast.ARTEM to artem,
            Cast.BABA_IRA to babaIra,
            Cast.ROMA to roma,
            Cast.BABA_LUBA to babaLuba,
            Cast.HABIB to habib,
            Cast.DEDA_MISHA to dedaMisha,
            Cast.UCHITEL to uchitel,
            Cast.MATVEY to matvey,
            Cast.ILYA to ilya,
        )
    }
}
