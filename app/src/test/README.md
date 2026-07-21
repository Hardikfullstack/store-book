# UI Snapshot Testing with Roborazzi

This project uses [Roborazzi](https://github.com/takahirom/roborazzi) for fast, headless, and deterministic screenshot testing to prevent UI regressions.

## How to add a new Snapshot Test
1. Create a file ending with `SnapshotTest.kt` inside `app/src/test/java/.../ui/screens/`.
2. Annotate the class with `@RunWith(RobolectricTestRunner::class)` and `@GraphicsMode(GraphicsMode.Mode.NATIVE)`.
3. Set the application class to default Android application to avoid triggering production side-effects like WorkManager: `@Config(sdk = [33], qualifiers = RobolectricDeviceQualifiers.Pixel5, application = android.app.Application::class)`
4. Use `composeTestRule.onRoot().captureRoboImage("src/test/snapshots/YourComponent_variant.png")`.

## How to record / regenerate Goldens
If you intentionally change the UI, you need to update the baseline (golden) images.
Run the following gradle task:
```bash
./gradlew recordRoborazziDebug
```
Then review the changes in Git to ensure only the expected pixels changed. Commit the new `.png` files.

## How to verify (CI Check)
On CI, or locally before you push, you can verify your UI against the baselines:
```bash
./gradlew verifyRoborazziDebug
```
If a snapshot differs by more than the allowed pixel threshold, the test will fail and output a diff image in `app/build/outputs/roborazzi/`.

## Ensuring Determinism
To avoid CI flakes:
- Animations should be disabled.
- Do not use `System.currentTimeMillis()` in UI; use injected Clocks so tests render the same date (e.g. 1 Jan 2024).
- Tests should lock the Locale to avoid differences between developer machines.

## Pixel-Diff Tolerance
To prevent CI from being flaky due to minor rendering differences, we use a global pixel-diff threshold of 0.5%.
This means a snapshot must deviate by more than **0.5%** of its total pixels before it fails the test.

You can configure this manually in your tests using:
```kotlin
captureRoboImage(
    roborazziOptions = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.005f)
    )
)
```
Or define it globally inside your Gradle configuration via the `roborazzi` plugin block or `gradle.properties` if supported by your version.
