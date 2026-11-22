# Mono PlayKit

**Your Games. Your Tools.**

Mono PlayKit is an open-source, 100% free, ad-free, offline-first Android companion toolkit for physical games, board games, puzzles, card games, sports, and tabletop toys—built with Jetpack Compose and the Mono Black & Grey design system.

## Included Utilities

- **Scoreboard & Sports:** Universal Scoreboard with presets for Basketball, Volleyball, Badminton, Table Tennis, Tennis, Football, Boxing, Darts (301/501), and Custom rules.
- **Board Games:** Chess Clock (Bullet, Blitz, Rapid, Fischer Increment/Delay, Chess960 & move notation), Monopoly Bank, Scrabble Utility, Ludo Companion, Snakes & Ladders, Dominoes Calculator, Bingo Caller, Jenga Block Selector, and Tangram Guide.
- **Puzzles & Solvers:** WCA Rubik's Cube Timer (15s inspection, scrambles, Ao5/Ao12/PB), interactive 3x3 Cube Solver, Pyraminx Solver, Skewb Solver, and Twisty Puzzle Guides.
- **Dice & Randomizers:** Polyhedral Dice Roller (D4–D20 & custom), Coin Flip, Random Number Generator, and Player/Team/Turn Picker.
- **Timers & Counters:** Stopwatch, Countdown Timer, Interval Timer, and Multi-Counter & Turns tracker.
- **Card Games:** TCG Life & Status Counter Hub and Playing Card Picker/Shuffler.
- **Physical Toys:** Fidget Spinner RPM/Duration Tracker, Yo-Yo & Kendama Trackers, LEGO Brick Inventory, Model Kit Checklist, and Metronome (30–260 BPM).

## Tech Stack

- **Language:** Kotlin + Jetpack Compose (Material 3)
- **Local Persistence:** Room SQLite + Jetpack DataStore (JSON backup export/import)
- **Architecture:** MVVM + Coroutines / StateFlow
- **CI/CD:** GitHub Actions automated APK builds & public Releases

## Automated Releases

Every push to `main` runs `.github/workflows/build-apk.yml` (AGP `9.1.1`, Java `17`, Gradle `9.3.1`), automatically building `MonoPlayKit-v<versionName>.apk` and publishing it to **GitHub Releases** with commit changelog notes.

## License

Licensed under the [MIT License](LICENSE).
