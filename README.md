# ⏱️ PlayTimePulse

A lightweight, accurate Minecraft plugin that tracks player playtime, integrates seamlessly with PlaceholderAPI, and provides a built-in rewards system with a claimable GUI.

---

## 🌟 Main Features

- ⏳ **Real-time Playtime Tracking** - Accurately tracks how long each player has been on your server
- 🎁 **24+ Playtime Rewards** - Milestone-based rewards that players can claim as they play more
- 📖 **Pageable Rewards GUI** - Easy-to-navigate interface with multiple pages for all rewards
- 🧾 **Simple Commands** - Intuitive `/playtime` command for players and admin tools
- 🔌 **PlaceholderAPI Support** - Integrates with popular plugins through placeholders
- 🛡️ **AFK Protection** - Prevents players from cheating by being AFK
- 💾 **Efficient Data Storage** - Lightweight flat-file storage with automatic saving
- ⚡ **High Performance** - Optimized code that won't slow down your server
- 🛡️ **Reward Error Handling** - Prevents reward spamming even when commands fail
- ⚙️ **Flexible Configuration** - Easily customize all rewards, GUI, and settings
- 🌍 **Multi-Language Support** - Supports 8 languages including English, Chinese, Russian, German, French, and more
- 🔧 **Admin Tools** - Reload configuration and debug mode for easier management

## 📦 All Features

- ⏳ Real-time playtime tracking (saved + current session)
- 🧾 `/playtime` command:
  - Shows formatted playtime in `Xd Yh Zm`
  - Opens a GUI to claim playtime-based rewards
- 🎁 Reward System:
  - Milestone-based claimable rewards (24 rewards included in current config, with more to be added over time)
  - Supports commands (since most of the things are given by commands)
  - Blocks duplicate claiming with tracked saves
  - Player head support for GUI icons (e.g., custom heads, player skulls)
  - Custom sound effects when claiming rewards
  - Error handling for failed commands to prevent reward spamming
- 📖 Pageable Rewards GUI:
  - Pagination support for large numbers of rewards
  - Customizable slots per page
  - Navigation buttons for moving between pages
  - "More Rewards Coming Soon" item displayed on last page
- 🧭 Navigation System:
  - Previous/Next page buttons
  - Close button to exit the GUI
  - Customizable navigation button materials and names
  - Optional custom head textures for navigation buttons
- 🛡️ AFK Protection:
  - Prevents AFK farming of playtime
  - Configurable interaction threshold and time window
  - Tracks meaningful player interactions to validate activity
- ⚠️ Configuration Validation:
  - Detailed error logging for misconfigured rewards
  - Detection and warning of nested reward configurations (indentation errors)
  - Slot conflict detection for rewards on the same page
- 🔌 Full PlaceholderAPI support
- 📊 bStats integration for anonymous usage statistics
- 📁 Flat-file storage with autosave
- ⚙️ Performance optimized and easy to configure
- 🌍 Multi-language support:
  - 8 languages supported (English, Chinese, Brazilian Portuguese, Russian, Filipino, German, French, Finnish)
  - Easily extensible with custom language files
  - Language setting in config.yml
- 🔧 Admin tools:
  - `/playtime reload` command to reload configuration without restarting
  - Debug mode for troubleshooting with detailed logging
  - `/playtime debug` command to toggle debug mode

---

## 🔧 Commands

| Command                                        | Description                                 | Permission     |
|------------------------------------------------|---------------------------------------------|----------------|
| `/playtime`                                    | Shows playtime and rewards GUI              | playtime.use   |
| `/playtime add <player> <hours>h [<minutes>m]` | Add playtime to player                      | playtime.admin |
| `/playtime reset <player>`                     | Reset player's playtime and claimed rewards | playtime.admin |
| `/playtime reload`                             | Reload plugin configuration                 | playtime.admin |
| `/playtime debug [true/false]`                 | Toggle debug mode (shows additional logs)   | playtime.admin |

---

## 🧩 PlaceholderAPI Support

| Placeholder                    | Description                                              |
|--------------------------------|----------------------------------------------------------|
| `%playtime_overall_formatted%` | Total time (saved + current), formatted like `2d 4h 30m` |
| `%playtime_formatted%`         | Current session only, formatted                          |
| `%playtime_overall%`           | Total time in seconds (saved + current)                  |
| `%playtime_saved%`             | Saved playtime only (in seconds)                         |
| `%playtime_current%`           | Current online session (in seconds)                      |
| `%playtime_seconds%`           | Seconds part of total playtime                           |
| `%playtime_minutes%`           | Minutes part of total playtime                           |
| `%playtime_hours%`             | Hours part of total playtime                             |
| `%playtime_days%`              | Days part of total playtime                              |

### 🎁 Advanced Reward Placeholders

| Placeholder                             | Description                                              |
|-----------------------------------------|----------------------------------------------------------|
| `%playtime_required%`                   | Time required for next unclaimed reward                  |
| `%playtime_required_{rewardId}%`        | Status of specific reward (Claimed/Ready/Not Claimed)    |
| `%playtime_time_left%`                  | Time left for next reward                                |
| `%playtime_time_left_{rewardId}%`       | Time left for specific reward                            |
| `%playtime_claimable_rewards%`          | Number of rewards that can be claimed right now          |

### 📋 Placeholder Examples

1. **Basic Playtime Display**:
   - `%playtime_overall_formatted%` → `2d 4h 30m`
   - `%playtime_days%` → `2`

2. **Reward Status Tracking**:
   - `%playtime_required%` → `5h 30m` (time for next reward)
   - `%playtime_required_level_5%` → `Claimed` (status of specific reward)
   - `%playtime_time_left_level_5%` → `2h 15m` (time left for specific reward)
   - `%playtime_claimable_rewards%` → `3` (number of claimable rewards)

3. **Integration Examples**:
   - In chat: "You've played for %playtime_overall_formatted%!"
   - In holograms: "Next reward in: %playtime_time_left%"
   - In scoreboards: "Claimable Rewards: %playtime_claimable_rewards%"

### 🛠️ Placeholder Usage Tips

1. **Reward IDs**:
   - Use the exact ID from your config.yml
   - Case-sensitive (level_5 ≠ Level_5)
   - Replace spaces with underscores

2. **Formatting**:
   - All time values are formatted as `Xd Yh Zm Ws`
   - Empty values are omitted (e.g., `2h 30s` instead of `0d 2h 0m 30s`)

3. **Error Handling**:
   - Returns `N/A` if PlaceholderAPI integration fails
   - Returns `Reward not found` for invalid reward IDs
   - Returns `All rewards claimed` when no more rewards are available

---

## 🧪 Future Plans

- 🎨 Gradient color support for formatted text
- 🌟 Particle effects for reward claiming
- 🐛 Debug mode for easier troubleshooting
- ➕ More features coming soon

---

## 📁 Configuration

- `config.yml`: Define playtime milestones, rewards (commands), requirements, rewards icon/lore, and sound effects.
  - Each reward can have a custom sound when claimed using the `claimSound` parameter
  - Choose from over 400 Minecraft sounds (e.g., `entity.player.levelup`, `block.note_block.pling`)
  - All sounds play at standard volume (1.0) and pitch (1.0)
  - Custom heads can be added using `headId` with Base64 texture values
  - Specify which page a reward should appear on using the `page` parameter (starting from 0)
  - Navigation buttons can use custom player heads with `headId` parameter
  - GUI slots and slots per page are configurable
  - AFK protection settings:
    - `interaction-threshold`: Minimum number of interactions required in the time window
    - `time-window-minutes`: Time window in minutes to check for interactions
  - Language settings:
    - `language`: Set the plugin language (en, zh, pt_BR, ru, fil, de, fr, fi)
    - Language files automatically copied to plugin directory for customization
  - Debug mode:
    - `debug`: Enable/disable debug mode for additional logging
  - Configuration validation:
    - Automatic detection of nested reward configurations (indentation errors)
    - Helpful warning messages for fixing configuration issues
    - Slot conflict detection to prevent rewards from overlapping

---

## 📥 Installation

1. Download the latest release
2. Drop the `.jar` file into your `/plugins` folder
3. Restart the server
4. Configure `config.yml` to define rewards / requirements.
5. (Optional) Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholders

---

## ✅ Requirements

- Minecraft: `1.21 up to 1.21.8`
- Java: `21+`
- Server: Paper / Pufferfish / Purpur (others need testing)
- (Optional) PlaceholderAPI

---

## 📊 Statistics

PlayTimePulse collects anonymous usage statistics through bStats. This helps us understand how the plugin is used and guides future development.

You can view plugin statistics at [bStats](https://bstats.org/plugin/bukkit/PlayTimePulse/26638).

To opt out of statistics collection, set `enabled: false` in the `/plugins/bStats/config.yml` file.

---

## 💬 Support

- 📬 [Open an issue](https://github.com/Souldevhub/PlayTimePulse/issues)
- 💻 [Discord](https://discord.gg/6SCAZfENjw)