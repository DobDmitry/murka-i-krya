package murka.core

/** Два персонажа игры: котик МУРКА и утёнок КРЯ. */
enum class Player {
    MURKA,
    KRYA;

    val opponent: Player
        get() = if (this == MURKA) KRYA else MURKA
}
