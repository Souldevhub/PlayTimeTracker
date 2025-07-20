# ⏱️ PlayTimeTracker

A lightweight, accurate Minecraft plugin that tracks player playtime, integrates seamlessly with PlaceholderAPI, and provides a built-in rewards system with a claimable GUI.

---

## 📦 Features

- ⏳ Real-time playtime tracking (saved + current session)
- 🧾 `/playtime` command:
  - Shows formatted playtime in `Xd Yh Zm`
  - Opens a GUI to claim playtime-based rewards
- 🎁 Reward System:
  - Milestone-based claimable rewards
  - Supports commands (since most of things are given by commands)
  - Blocks duplicate claiming with tracked saves
  - Player head support for GUI icons (e.g., custom heads, player skulls)
- 🔌 Full PlaceholderAPI support
- 📁 Flat-file storage with autosave
- ⚙️ Performance optimized and easy to configure

---

## 🔧 Commands

| Command          | Description                             | Permission |
|------------------|-----------------------------------------|----------|
| `/playtime`      | Shows playtime and opens rewards GUI    |    -     |

---

## 🧩 PlaceholderAPI Support

| Placeholder                      | Description                                               |
|----------------------------------|-----------------------------------------------------------|
| `%playtime_overall_formatted%`  | Total time (saved + current), formatted like `2d 4h 30m`  |
| `%playtime_formatted%`          | Current session only, formatted                          |
| `%playtime_overall%`            | Total time in seconds (saved + current)                  |
| `%playtime_saved%`              | Saved playtime only (in seconds)                         |
| `%playtime_current%`            | Current online session (in seconds)                      |

---

## 🧪 Future Plans

- 🎨 Gradient color support for formatted text
- 🔊 Customizable sounds and particle effects
- 🎯 Configurable GUI slots for rewards

---

## 📁 Configuration

- `config.yml`: Define playtime milestones, rewards (commands), requirements, rewards icon/lore.

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

## 💬 Support

- 📬 [Open an issue](https://github.com/Souldevhub/PlayTimeTracker/issues)
- 💻 [Discord](https://discord.gg/6SCAZfENjw)

---

## 📄 License

Licensed under the GNU General Public License v3.0 (GPLv3)  
See the [LICENSE](LICENSE) file for details.
