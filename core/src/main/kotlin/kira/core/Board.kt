package kira.core

/**
 * Неизменяемое поле 3x3. Клетки нумеруются слева направо, сверху вниз:
 *
 * ```
 * 0 1 2
 * 3 4 5
 * 6 7 8
 * ```
 */
class Board private constructor(private val cells: Array<Side?>) {

    operator fun get(index: Int): Side? = cells[index]

    fun isEmpty(index: Int): Boolean = cells[index] == null

    val isFull: Boolean
        get() = cells.all { it != null }

    val moveCount: Int
        get() = cells.count { it != null }

    fun emptyCells(): List<Int> = (0 until SIZE).filter { cells[it] == null }

    /** Возвращает новое поле с поставленной фигурой. Занятую клетку занять нельзя. */
    fun withMove(index: Int, player: Side): Board {
        require(index in 0 until SIZE) { "Клетки $index не существует" }
        require(cells[index] == null) { "Клетка $index уже занята" }
        val next = cells.copyOf()
        next[index] = player
        return Board(next)
    }

    /** Компактная запись поля: 9 символов, 'A' — первый игрок, 'B' — второй, '.' — пусто. */
    fun code(): String = buildString {
        for (cell in cells) {
            append(
                when (cell) {
                    Side.FIRST -> 'A'
                    Side.SECOND -> 'B'
                    null -> '.'
                }
            )
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is Board && cells.contentEquals(other.cells))

    override fun hashCode(): Int = cells.contentHashCode()

    override fun toString(): String = code().chunked(3).joinToString("/")

    companion object {
        const val SIZE = 9

        fun empty(): Board = Board(arrayOfNulls(SIZE))

        /** Разбирает компактную запись, см. [code]. Пробелы и переводы строк игнорируются. */
        fun of(code: String): Board {
            val clean = code.filterNot { it.isWhitespace() }
            require(clean.length == SIZE) { "Нужно ровно $SIZE клеток, получено ${clean.length}" }
            val cells = arrayOfNulls<Side>(SIZE)
            clean.forEachIndexed { index, symbol ->
                cells[index] = when (symbol) {
                    'A', 'a' -> Side.FIRST
                    'B', 'b' -> Side.SECOND
                    '.', '-', '_' -> null
                    else -> throw IllegalArgumentException("Непонятный символ '$symbol'")
                }
            }
            return Board(cells)
        }
    }
}
