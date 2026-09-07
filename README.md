# Bookend

An Android journaling app that prompts you at both ends of the day and asks the
same five questions each time:

1. **High** — the best part of the day
2. **Low** — the hardest part
3. **Grateful for**
4. **Learned**
5. **To work on**

In the morning the questions look forward ("What are you most looking forward to
today?"); in the evening they look back ("What was the high point of your day?").
Each day therefore holds up to two entries — a morning one and an evening one —
so you can see what you expected against what actually happened.

## Features

- Two daily notifications, at times you choose (default 7:00 AM and 9:00 PM).
  Tapping one opens straight into that half of the day.
- A home-screen widget showing the next question you have not answered, with a
  tap target for each half of the day.
- Answers save as you type; an entry with every field cleared is removed.
- History grouped by day, with a streak count on the home screen.
- Everything is stored locally in a Room database. No account, no network
  permission, no analytics.

## Installing a build

Every push builds an APK and attaches it to the `dev` prerelease:

    https://github.com/lukethedu91/Sub-Compiler/releases/download/dev/bookend-debug.apk

Open that on the phone and allow your browser to install unknown apps. Each CI
run signs with a fresh debug key, so uninstall the previous copy before
installing a newer one.

## Building

```
./gradlew assembleDebug          # debug APK in app/build/outputs/apk/debug/
./gradlew testDebugUnitTest      # unit tests
```

Requires JDK 17 and the Android SDK (compileSdk 35). `minSdk` is 26.

## Project layout

```
app/src/main/java/com/bookend/reflection/
├── BookendApp.kt              application: repositories, channel, alarm boot-up
├── data/                      Room entities, DAO, repositories, settings, streak math
├── reminder/                  alarm scheduling, notifications, boot/time-change receiver
├── widget/                    home-screen quick entry widget
└── ui/                        Compose screens (home, entry, history, settings)
```

Reminders use `AlarmManager.setAndAllowWhileIdle`, which needs no exact-alarm
permission; each firing books the next day's alarm, and `BootReceiver` re-books
both after a reboot, an app update, or a time-zone change.

## Notes

The five questions and their morning/evening wording live in one place —
`data/Question.kt` — if you want to change or reword them.
