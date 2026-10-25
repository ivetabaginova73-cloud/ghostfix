# Ghostfix (Fabric, Minecraft 1.21.11)

Client-side mod that reduces "ghost hits" when chasing someone (especially mid-air).

## What it does
Vanilla throws away a click if your crosshair is on nothing for that exact tick, and it also
resets your attack cooldown. Ghostfix remembers the missed click for 1-5 ticks and fires it
as soon as the crosshair is on a valid target again.

It does NOT change reach or hitboxes.

## Settings (press Right Shift in game, changeable in Controls)
- Quick presets: Light / Balanced / Strong
- Ghostfix on/off
- Click memory (1-5 ticks)
- Only players, only mid-air targets
- Wait for attack cooldown + how much cooldown is needed
- Reset to defaults

Hover any option for an explanation. Settings are saved in `config/ghostfix.json`.

## Get Ghostfix.jar (no coding needed)

### Option A - build online with GitHub (easiest)
1. Make a free account at github.com, click "New repository", name it anything, create it.
2. Click "uploading an existing file", drag in EVERYTHING from this folder
   (including the hidden `.github` folder), then click "Commit changes".
3. Open the "Actions" tab. Wait 3-5 minutes for "Build Ghostfix.jar" to get a green tick.
4. Click that run, scroll down to "Artifacts", download "Ghostfix". Unzip it: inside is Ghostfix.jar.

### Option B - build on your PC
1. Install Java 21 (Temurin) and Gradle 9.2.1.
2. Open a terminal in this folder and run: `gradle build`
3. The mod is `build/libs/Ghostfix.jar`.

## Install
Put Ghostfix.jar in `.minecraft/mods` together with Fabric Loader and Fabric API for 1.21.11.
