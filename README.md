# TikTok Auto Scroll (Android, Java)

Accessibility + floating overlay app that auto-swipes TikTok when a video ends.

## Features
- Runs on top of official TikTok (`com.zhiliaoapp.musically`).
- Floating control with:
  - Start/Pause auto-scroll
  - Switch swipe direction (Up/Down)
- Detects end-of-video using visible countdown/time text in TikTok's UI.

## Setup
1. Open app.
2. Grant overlay permission.
3. Enable accessibility service for this app.
4. Open TikTok and enable `Start` in floating control.

## Notes
TikTok UI can vary by version/region/device. If no time text is exposed to accessibility tree, end detection may require a per-device heuristic.
