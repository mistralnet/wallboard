# Wallboard

A lightweight Android TV app that displays a single web page fullscreen — for use as a wall display / digital signage board.

## Features
- Full immersive fullscreen, no browser chrome.
- Configurable page URL (set in the in-app settings screen).
- Configurable auto-refresh interval (off / 1 / 3 / 6 / 12 / 24 hours).
- Refresh on resume + manual refresh.
- Display zoom control (75–125%).
- Optional start-on-boot.
- In-app updater: pulls the latest build from a configurable APK URL.

## Opening settings
On the remote: **long-press the Back button** (or press MENU). An on-screen settings button also appears when you press any D-pad key.

## Build
Builds run automatically via GitHub Actions on every push to `main`, producing a signed `wallboard.apk` published to the **latest** release:

```
https://github.com/mistralnet/wallboard/releases/latest/download/wallboard.apk
```

## Install (sideload)
Point the in-app updater at the release URL above, or install the APK once via a sideload helper app on the TV.
