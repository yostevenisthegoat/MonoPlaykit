package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FilterNone
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Scoreboard
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.material.icons.outlined.Toys
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class UtilityCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    @param:DrawableRes val bannerResId: Int,
) {
    SCOREBOARD(
        id = "scoreboard",
        title = "Scoreboard & Sports",
        subtitle = "Universal match tracker with 9 sport presets",
        icon = Icons.Outlined.Scoreboard,
        bannerResId = R.drawable.img_hero_scoreboard_1795355380624,
    ),
    BOARD_GAMES(
        id = "board_games",
        title = "Board Games",
        subtitle = "Chess clocks, Monopoly bank, Scrabble & tabletop",
        icon = Icons.Outlined.Extension,
        bannerResId = R.drawable.img_hero_boardgames_1795355382325,
    ),
    PUZZLES(
        id = "puzzles",
        title = "Puzzles & Solvers",
        subtitle = "Cube timers, 3x3/Pyraminx/Skewb solvers & guides",
        icon = Icons.Outlined.ViewInAr,
        bannerResId = R.drawable.img_hero_puzzles_1795355381347,
    ),
    DICE_RANDOMIZERS(
        id = "dice_randomizers",
        title = "Dice & Randomizers",
        subtitle = "Polyhedral dice, coin flip, RNG & team pickers",
        icon = Icons.Outlined.Casino,
        bannerResId = R.drawable.img_hero_dice_toys_1795355381301,
    ),
    TIMERS_COUNTERS(
        id = "timers_counters",
        title = "Timers & Counters",
        subtitle = "Stopwatch, countdown, intervals & turn tallies",
        icon = Icons.Outlined.Timer,
        bannerResId = R.drawable.img_hero_scoreboard_1795355380624,
    ),
    CARD_GAMES(
        id = "card_games",
        title = "Card Games",
        subtitle = "TCG life & status hub, shuffler & card picker",
        icon = Icons.Outlined.Style,
        bannerResId = R.drawable.img_hero_boardgames_1795355382325,
    ),
    PHYSICAL_TOYS(
        id = "physical_toys",
        title = "Physical Toys",
        subtitle = "Spinner RPM, Yo-Yo, Kendama, LEGO & Metronome",
        icon = Icons.Outlined.Toys,
        bannerResId = R.drawable.img_hero_dice_toys_1795355381301,
    ),
}

data class UtilityTool(
    val id: String,
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val category: UtilityCategory,
    val subCategory: String,
    val icon: ImageVector,
    @param:DrawableRes val visualResId: Int = category.bannerResId,
    val keywords: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val isQuickUtility: Boolean = false,
)

object UtilityIds {
    const val SCOREBOARD = "scoreboard"
    const val CHESS_CLOCK = "chess_clock"
    const val DICE_ROLLER = "dice_roller"
    const val CUBE_TIMER = "cube_timer"
    const val STOPWATCH = "stopwatch"
    const val COIN_FLIP = "coin_flip"

    const val CUBE_SOLVER = "cube_solver"
    const val PYRAMINX_SOLVER = "pyraminx_solver"
    const val SKEWB_SOLVER = "skewb_solver"
    const val PUZZLE_GUIDES = "puzzle_guides"

    const val MONOPOLY_BANK = "monopoly_bank"
    const val SCRABBLE_TOOL = "scrabble_tool"
    const val LUDO_TOOL = "ludo_tool"
    const val SNAKES_LADDERS = "snakes_ladders"
    const val DOMINOES_CALC = "dominoes_calc"
    const val BINGO_CALLER = "bingo_caller"
    const val JENGA_SELECTOR = "jenga_selector"
    const val TANGRAM_GUIDE = "tangram_guide"

    const val RNG_TOOL = "rng_tool"
    const val TEAM_RANDOMIZER = "team_randomizer"

    const val TCG_COMPANION = "tcg_companion"
    const val CARD_PICKER = "card_picker"

    const val FIDGET_SPINNER = "fidget_spinner"
    const val YOYO_TRACKER = "yoyo_tracker"
    const val KENDAMA_TRACKER = "kendama_tracker"
    const val LEGO_INVENTORY = "lego_inventory"
    const val MODEL_KIT_CHECKLIST = "model_kit_checklist"
    const val METRONOME = "metronome"

    const val COUNTDOWN_TIMER = "countdown_timer"
    const val INTERVAL_TIMER = "interval_timer"
    const val MULTI_COUNTER = "multi_counter"
}

object UtilityCatalog {
    val allTools: List<UtilityTool> = listOf(
        // Scoreboard
        UtilityTool(
            id = UtilityIds.SCOREBOARD,
            title = "Universal Scoreboard",
            shortTitle = "Scoreboard",
            subtitle = "Basketball, Volleyball, Tennis, Darts, Boxing & Custom",
            category = UtilityCategory.SCOREBOARD,
            subCategory = "Match & Sport Scoring",
            icon = Icons.Outlined.Scoreboard,
            visualResId = R.drawable.img_hero_scoreboard_1795355380624,
            keywords = listOf(
                "score", "basketball", "volleyball", "badminton", "table tennis",
                "ping pong", "tennis", "football", "soccer", "boxing", "darts", "301", "501", "match"
            ),
            isFeatured = true,
            isQuickUtility = true,
        ),
        // Puzzles
        UtilityTool(
            id = UtilityIds.CUBE_TIMER,
            title = "Rubik's Cube Timer",
            shortTitle = "Cube Timer",
            subtitle = "WCA inspection, scramble generator, Ao5, Ao12 & PB",
            category = UtilityCategory.PUZZLES,
            subCategory = "Speedcubing",
            icon = Icons.Outlined.Timer,
            visualResId = R.drawable.img_hero_puzzles_1795355381347,
            keywords = listOf("rubiks", "speedcube", "wca", "ao5", "ao12", "scramble", "3x3", "2x2", "4x4"),
            isFeatured = true,
            isQuickUtility = true,
        ),
        UtilityTool(
            id = UtilityIds.CUBE_SOLVER,
            title = "Rubik's Cube Solver (3x3)",
            shortTitle = "3x3 Solver",
            subtitle = "Interactive 3x3 input, validation & step-by-step CFOP solution",
            category = UtilityCategory.PUZZLES,
            subCategory = "Algorithmic Solvers",
            icon = Icons.Outlined.ViewInAr,
            keywords = listOf("rubik", "solver", "3x3", "cfop", "algorithm", "notation"),
        ),
        UtilityTool(
            id = UtilityIds.PYRAMINX_SOLVER,
            title = "Pyraminx Solver",
            shortTitle = "Pyraminx",
            subtitle = "Interactive Pyraminx state solver with optimal move steps",
            category = UtilityCategory.PUZZLES,
            subCategory = "Algorithmic Solvers",
            icon = Icons.Outlined.Contrast,
            keywords = listOf("pyraminx", "triangle", "pyramid", "solver"),
        ),
        UtilityTool(
            id = UtilityIds.SKEWB_SOLVER,
            title = "Skewb Solver",
            shortTitle = "Skewb",
            subtitle = "Corner-turning Skewb solver with step-by-step notation",
            category = UtilityCategory.PUZZLES,
            subCategory = "Algorithmic Solvers",
            icon = Icons.Outlined.GridView,
            keywords = listOf("skewb", "solver", "twisty"),
        ),
        UtilityTool(
            id = UtilityIds.PUZZLE_GUIDES,
            title = "Twisty Puzzle Guides",
            shortTitle = "Puzzle Guides",
            subtitle = "Megaminx, Square-1, Mirror Cube, Gear Cube & Clock guides",
            category = UtilityCategory.PUZZLES,
            subCategory = "Reference Guides",
            icon = Icons.Outlined.Dashboard,
            keywords = listOf("megaminx", "square-1", "sq1", "mirror cube", "gear cube", "rubik clock", "guide", "parity"),
        ),
        // Board Games
        UtilityTool(
            id = UtilityIds.CHESS_CLOCK,
            title = "Chess Clock",
            shortTitle = "Chess Clock",
            subtitle = "Bullet, Blitz, Rapid, Increment, Delay, Notation & Chess960",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Chess & Timing",
            icon = Icons.Outlined.HourglassBottom,
            visualResId = R.drawable.img_hero_boardgames_1795355382325,
            keywords = listOf("chess", "bullet", "blitz", "rapid", "classical", "fischer", "960", "notation", "clock"),
            isFeatured = true,
            isQuickUtility = true,
        ),
        UtilityTool(
            id = UtilityIds.MONOPOLY_BANK,
            title = "Monopoly Bank",
            shortTitle = "Monopoly",
            subtitle = "Digital banker, player balances, rent calculator & ledger",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Classic Board",
            icon = Icons.Outlined.Money,
            keywords = listOf("monopoly", "bank", "rent", "money", "transactions", "go"),
        ),
        UtilityTool(
            id = UtilityIds.SCRABBLE_TOOL,
            title = "Scrabble Utility",
            shortTitle = "Scrabble",
            subtitle = "Word score calculator, 2L/3L/2W/3W, turn timer & tile bag",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Classic Board",
            icon = Icons.Outlined.Spellcheck,
            keywords = listOf("scrabble", "word", "tiles", "letter", "bingo", "score"),
        ),
        UtilityTool(
            id = UtilityIds.LUDO_TOOL,
            title = "Ludo Companion",
            shortTitle = "Ludo",
            subtitle = "1d6 roll-again tracker, 3-sixes rule & 4-player turn order",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Classic Board",
            icon = Icons.Outlined.PlayCircleOutline,
            keywords = listOf("ludo", "parcheesi", "turns", "dice"),
        ),
        UtilityTool(
            id = UtilityIds.SNAKES_LADDERS,
            title = "Snakes & Ladders",
            shortTitle = "Snakes & Ladders",
            subtitle = "1 or 2 dice mode, roll log & multi-player turn positions",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Classic Board",
            icon = Icons.Outlined.SwapHoriz,
            keywords = listOf("snakes", "ladders", "chutes", "dice", "board"),
        ),
        UtilityTool(
            id = UtilityIds.DOMINOES_CALC,
            title = "Dominoes Calculator",
            shortTitle = "Dominoes",
            subtitle = "Pip counter pad, multi-player rounds & target score tracker",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Tile, Number & Shape",
            icon = Icons.Outlined.FilterNone,
            keywords = listOf("domino", "dominoes", "pips", "muggins", "all fives"),
        ),
        UtilityTool(
            id = UtilityIds.BINGO_CALLER,
            title = "Bingo Caller",
            shortTitle = "Bingo",
            subtitle = "75-ball & 90-ball caller, duplicate prevention & board grid",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Tile, Number & Shape",
            icon = Icons.Outlined.ConfirmationNumber,
            keywords = listOf("bingo", "75", "90", "caller", "balls", "housie"),
        ),
        UtilityTool(
            id = UtilityIds.JENGA_SELECTOR,
            title = "Jenga Block Selector",
            shortTitle = "Jenga",
            subtitle = "Random block number, layer/position & custom range",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Tile, Number & Shape",
            icon = Icons.Outlined.FormatListNumbered,
            keywords = listOf("jenga", "block", "tower", "number"),
        ),
        UtilityTool(
            id = UtilityIds.TANGRAM_GUIDE,
            title = "Tangram Guide",
            shortTitle = "Tangram",
            subtitle = "Geometric silhouette references & 7-piece solution overlays",
            category = UtilityCategory.BOARD_GAMES,
            subCategory = "Tile, Number & Shape",
            icon = Icons.Outlined.PieChart,
            keywords = listOf("tangram", "shapes", "silhouette", "puzzle", "geometry"),
        ),
        // Dice & Randomizers
        UtilityTool(
            id = UtilityIds.DICE_ROLLER,
            title = "Dice Roller",
            shortTitle = "Dice Roller",
            subtitle = "D4, D6, D8, D10, D12, D20, custom dice & roll history",
            category = UtilityCategory.DICE_RANDOMIZERS,
            subCategory = "Dice & Coin",
            icon = Icons.Outlined.Casino,
            visualResId = R.drawable.img_hero_dice_toys_1795355381301,
            keywords = listOf("dice", "d4", "d6", "d8", "d10", "d12", "d20", "dnd", "rpg", "roll"),
            isFeatured = true,
            isQuickUtility = true,
        ),
        UtilityTool(
            id = UtilityIds.COIN_FLIP,
            title = "Coin Flip",
            shortTitle = "Coin Flip",
            subtitle = "Heads or Tails with streak tracking & probability stats",
            category = UtilityCategory.DICE_RANDOMIZERS,
            subCategory = "Dice & Coin",
            icon = Icons.Outlined.Toll,
            keywords = listOf("coin", "flip", "toss", "heads", "tails"),
            isQuickUtility = true,
        ),
        UtilityTool(
            id = UtilityIds.RNG_TOOL,
            title = "Random Number Generator",
            shortTitle = "Random Number",
            subtitle = "Custom min-max range, multi-draw & unique mode",
            category = UtilityCategory.DICE_RANDOMIZERS,
            subCategory = "Pickers & Teams",
            icon = Icons.Outlined.Numbers,
            keywords = listOf("rng", "random", "number", "generator", "lottery"),
        ),
        UtilityTool(
            id = UtilityIds.TEAM_RANDOMIZER,
            title = "Player, Turn & Team Picker",
            shortTitle = "Team & Order",
            subtitle = "Pick random player, shuffle turn order or split teams",
            category = UtilityCategory.DICE_RANDOMIZERS,
            subCategory = "Pickers & Teams",
            icon = Icons.Outlined.Groups,
            keywords = listOf("player", "team", "turn order", "randomizer", "shuffle", "captain"),
        ),
        // Card Games
        UtilityTool(
            id = UtilityIds.TCG_COMPANION,
            title = "TCG Life & Counter Hub",
            shortTitle = "TCG Counters",
            subtitle = "Life points, damage, energy, poison, turns & status effects",
            category = UtilityCategory.CARD_GAMES,
            subCategory = "Trading Cards (TCG)",
            icon = Icons.Outlined.SportsEsports,
            keywords = listOf("tcg", "mtg", "magic", "yugioh", "pokemon", "life", "damage", "energy", "commander"),
        ),
        UtilityTool(
            id = UtilityIds.CARD_PICKER,
            title = "Playing Card Picker",
            shortTitle = "Card Picker",
            subtitle = "Deck config, Fisher-Yates shuffler & card draw history",
            category = UtilityCategory.CARD_GAMES,
            subCategory = "Standard Decks",
            icon = Icons.Outlined.Style,
            keywords = listOf("cards", "deck", "poker", "shuffle", "draw", "playing cards"),
        ),
        // Physical Toys
        UtilityTool(
            id = UtilityIds.FIDGET_SPINNER,
            title = "Fidget Spinner Tracker",
            shortTitle = "Spinner",
            subtitle = "Spin duration stopwatch, records & tap-measured RPM",
            category = UtilityCategory.PHYSICAL_TOYS,
            subCategory = "Skill Toys",
            icon = Icons.Outlined.Loop,
            keywords = listOf("fidget", "spinner", "rpm", "spin", "duration"),
        ),
        UtilityTool(
            id = UtilityIds.YOYO_TRACKER,
            title = "Yo-Yo Practice Tracker",
            shortTitle = "Yo-Yo",
            subtitle = "Trick counter, combo timer, trick library & personal bests",
            category = UtilityCategory.PHYSICAL_TOYS,
            subCategory = "Skill Toys",
            icon = Icons.Outlined.Refresh,
            keywords = listOf("yoyo", "yo-yo", "tricks", "practice", "sleeper", "trapeze"),
        ),
        UtilityTool(
            id = UtilityIds.KENDAMA_TRACKER,
            title = "Kendama Tracker",
            shortTitle = "Kendama",
            subtitle = "Spike streak counter, trick reference guide & sessions",
            category = UtilityCategory.PHYSICAL_TOYS,
            subCategory = "Skill Toys",
            icon = Icons.Outlined.Balance,
            keywords = listOf("kendama", "spike", "cups", "lighthouse", "lunar", "tricks"),
        ),
        UtilityTool(
            id = UtilityIds.LEGO_INVENTORY,
            title = "LEGO Brick & Build Tracker",
            shortTitle = "LEGO Tracker",
            subtitle = "Part inventory counts, building checklist & project progress",
            category = UtilityCategory.PHYSICAL_TOYS,
            subCategory = "Builders & Kits",
            icon = Icons.Outlined.CheckBox,
            keywords = listOf("lego", "bricks", "inventory", "checklist", "parts", "build"),
        ),
        UtilityTool(
            id = UtilityIds.MODEL_KIT_CHECKLIST,
            title = "Model Kit Checklist",
            shortTitle = "Model Kits",
            subtitle = "Sprue/runner checklist, build stages & personal notes",
            category = UtilityCategory.PHYSICAL_TOYS,
            subCategory = "Builders & Kits",
            icon = Icons.Outlined.Construction,
            keywords = listOf("model", "kit", "gunpla", "assembly", "checklist", "paint"),
        ),
        UtilityTool(
            id = UtilityIds.METRONOME,
            title = "Metronome & Practice",
            shortTitle = "Metronome",
            subtitle = "30–260 BPM click, tap tempo, time signatures & practice timer",
            category = UtilityCategory.PHYSICAL_TOYS,
            subCategory = "Musical Instruments",
            icon = Icons.Outlined.LibraryMusic,
            keywords = listOf("metronome", "bpm", "tempo", "music", "instrument", "practice"),
        ),
        // Timers & Counters
        UtilityTool(
            id = UtilityIds.STOPWATCH,
            title = "Stopwatch",
            shortTitle = "Stopwatch",
            subtitle = "High-precision monotonic stopwatch with split & lap history",
            category = UtilityCategory.TIMERS_COUNTERS,
            subCategory = "Precision Clocks",
            icon = Icons.Outlined.Timer,
            keywords = listOf("stopwatch", "lap", "split", "chronograph"),
            isQuickUtility = true,
        ),
        UtilityTool(
            id = UtilityIds.COUNTDOWN_TIMER,
            title = "Countdown Timer",
            shortTitle = "Countdown",
            subtitle = "Custom duration presets, progress ring & completion alert",
            category = UtilityCategory.TIMERS_COUNTERS,
            subCategory = "Precision Clocks",
            icon = Icons.Outlined.AccessAlarm,
            keywords = listOf("countdown", "timer", "alarm", "round timer"),
        ),
        UtilityTool(
            id = UtilityIds.INTERVAL_TIMER,
            title = "Interval Timer",
            shortTitle = "Intervals",
            subtitle = "Work/rest rounds with saved configurations & audio cues",
            category = UtilityCategory.TIMERS_COUNTERS,
            subCategory = "Precision Clocks",
            icon = Icons.Outlined.Loop,
            keywords = listOf("interval", "rounds", "work", "rest", "tabata", "drills"),
        ),
        UtilityTool(
            id = UtilityIds.MULTI_COUNTER,
            title = "Multi-Counter & Turns",
            shortTitle = "Counters",
            subtitle = "Named counters, custom steps, player turns & saved setups",
            category = UtilityCategory.TIMERS_COUNTERS,
            subCategory = "Tallies & Turns",
            icon = Icons.Outlined.Calculate,
            keywords = listOf("counter", "tally", "points", "turn tracker", "score calculator"),
        ),
    )

    val featuredTools: List<UtilityTool> = allTools.filter { it.isFeatured }
    val quickTools: List<UtilityTool> = allTools.filter { it.isQuickUtility }

    val sportShortcuts: List<Pair<String, String>> = listOf(
        "Basketball" to "Basketball",
        "Volleyball" to "Volleyball",
        "Badminton" to "Badminton",
        "Table Tennis" to "Table Tennis",
        "Tennis" to "Tennis",
        "Football" to "Football",
        "Boxing" to "Boxing",
        "Darts 501" to "501",
    )

    fun findById(id: String): UtilityTool? = allTools.find { it.id == id }

    fun byCategory(category: UtilityCategory): List<UtilityTool> =
        allTools.filter { it.category == category }

    fun search(query: String, categoryFilter: UtilityCategory? = null): List<UtilityTool> {
        val trimmed = query.trim().lowercase()
        return allTools.filter { tool ->
            val matchesCategory = categoryFilter == null || tool.category == categoryFilter
            val matchesQuery = trimmed.isEmpty() ||
                tool.title.lowercase().contains(trimmed) ||
                tool.shortTitle.lowercase().contains(trimmed) ||
                tool.subtitle.lowercase().contains(trimmed) ||
                tool.subCategory.lowercase().contains(trimmed) ||
                tool.category.title.lowercase().contains(trimmed) ||
                tool.keywords.any { it.contains(trimmed) }
            matchesCategory && matchesQuery
        }
    }

    val searchSuggestions: List<String> = listOf(
        "Chess",
        "Dice",
        "Basketball",
        "Cube",
        "Stopwatch",
        "Scoreboard",
        "Monopoly",
        "Scrabble",
        "Metronome",
        "Bingo",
    )
}
