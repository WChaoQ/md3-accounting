# Changelog

## 1.0.6 (2026-10-24)

- Replace the launcher artwork with the supplied SVG geometry and colors, without an extra foreground scale or translation.
- Update the top-bar data action to use the Material Symbols Outlined database glyph.
- Apply edge-to-edge system bar styles as a Compose side effect so the status/navigation bars track theme changes promptly.
- Preserve existing screens, layouts, and features.

## 1.0.5 (2026-10-24)

- Correct the launcher wordmark's optical vertical alignment.
- Keep the data-management action represented by a clear database glyph.
- Keep the existing screens and features unchanged.
- Publish installable debug and release APKs through GitHub Actions.

## 1.0.1 (2026-10-23)

- Improve the home calendar density and selected/today visual hierarchy.
- Improve statistics chart sizing and expense-detail readability.
- Consolidate appearance controls in the app bar.
- Improve the empty-day state guidance.
- Publish an installable CI APK for Android sideloading.

### Installation note

This CI APK is signed with the Android debug signing configuration for direct installation. It is intended for testing/sideloading, not Google Play distribution. If Android reports a signature conflict when updating, uninstall the previous CI build first; back up any important data before uninstalling.
