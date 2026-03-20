# Girlfriend Mod

A Fabric mod for Minecraft 1.21.11 that adds a companion girlfriend entity. She follows you, fights for you, and responds to chat. Each girlfriend has a random name from 500+ options and a random player skin from a fixed list.

## How to Use

- **Summon**: Use the Girlfriend Summoner item (from Ingredients creative tab or `/girlfriend give`) or run `/girlfriend summon` to spawn one at your feet. She gets a random name and skin.
- **Stats**: Stats show in many ways: name tag above the entity (two lines), HUD overlay when you look at her (center screen), action bar when you right-click her, `/girlfriend stats` (chat + action bar), `/girlfriend list` (prints each girlfriend’s stats), and the summoner item tooltip.
- **Interact**: Right-click to toggle follow/wait. Sneak + right-click for a hug. Feed her food or give flowers/valuables to raise affection and mood. Use a name tag to rename.
- **Chat**: Type in chat near her; she may reply. Only these chat replies use a short typing delay; hug and hit responses are instant.

## Commands

All commands affect **the girlfriend closest to you** (no username or entity UUID). You must be a player.

- `/girlfriend summon` – Spawn a girlfriend at your position (random name and skin).
- `/girlfriend give` – Give yourself a Girlfriend Summoner.
- `/girlfriend list` – Count your girlfriends within 128 blocks and show each one’s stats.
- `/girlfriend stats` – Show the closest girlfriend’s stats in chat and on the action bar.
- `/girlfriend relationship <0–100>` – Set relationship of closest girlfriend.
- `/girlfriend mood <0–100>` – Set mood of closest girlfriend.
- `/girlfriend texture <default|alt>` – Set texture variant of closest girlfriend.
- `/girlfriend skin <username>` – Set skin of closest girlfriend to that player’s username.

Requires Fabric Loader and Fabric API for Minecraft 1.21.11.
