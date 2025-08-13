# 📜 PlayTimePulse Changelog

## 🚀 Version 1.0.5-SNAPSHOT

### 🌟 New Features

#### 🎁 Reward System Customization
- **Customizable Reward Information Display** - Added `disable-default-reward-information` config option to control what information is automatically shown on reward items
  - When set to `false` (default), shows status, required time, and your time information on reward items
  - When set to `true`, only displays lore defined in the config file, allowing complete customization
  - This feature gives server owners full control over how reward information is displayed

#### 📖 Pageable Rewards GUI Improvements
- **Fixed Navigation Bug** - Resolved critical issue where navigation between pages was not working correctly in some cases
  - Players can now properly navigate between reward pages
  - "More Rewards Coming Soon" item now correctly displays only when there are truly no more pages with rewards

#### 🛠️ Configuration Improvements
- **Default Minecraft Rewards** - Updated levels 20-23 to use default Minecraft items and commands instead of plugin-specific ones
  - Ensures compatibility with any vanilla Minecraft server
  - Uses valuable Minecraft items like Enchanted Golden Apples, Totems of Undying, and Enchanted Books
  - No longer requires external plugins like economy or modifier plugins

### 🛠️ Improvements

#### ⚙️ Configuration
- **Cleaner Config Structure** - Removed unnecessary `clear-default-rewards` parameter since users can simply delete config themselves
- **Improved YAML Formatting** - Fixed indentation issues in config.yml for better readability and parsing

## 🚀 Version 1.0.4-SNAPSHOT

### 🌟 New Features

#### ⏱️ Real-time Tracking
- **Second-by-Second Updates** - Playtime now updates every second instead of every minute for ultra-precise tracking
- **Enhanced Accuracy** - More accurate playtime measurement for fair reward distribution

#### 🏆 Leaderboard Support
- **Top Player Placeholders** - Added placeholders for top 10 players (`%playtime_top_1%` through `%playtime_top_10%`)
- **Player Position Placeholder** - Added `%playtime_leaderboard_position%` to show a player's rank
- **Real-time Updates** - Leaderboard updates every second with playtime tracking

#### 🔧 Admin Tools
- **Reload Command** - Added `/playtime reload` command for administrators to reload configuration without restarting the server
- **Debug Mode** - Added debug mode with detailed logging for troubleshooting
  - Enable with `debug: true` in config.yml
  - Toggle with `/playtime debug [true/false]` command
  - Shows detailed information about reward processing and configuration loading

#### 🌍 Multi-Language Support
- **8 Languages Supported**:
  - 🇬🇧 English (`en`) - Default language
  - 🇨🇳 Chinese (`zh`)
  - 🇧🇷 Brazilian Portuguese (`pt_BR`)
  - 🇷🇺 Russian (`ru`)
  - 🇵🇭 Filipino (`fil`)
  - 🇩🇪 German (`de`)
  - 🇫🇷 French (`fr`)
  - 🇫🇮 Finnish (`fi`)
- **Language Configuration**:
  - Set language with `language: [code]` in config.yml
  - Language files automatically copied to plugin directory for customization
  - Fallback system: Language files → config translations → default English

### 🛠️ Improvements

#### ⏱️ Playtime Tracking
- **Second Precision** - All time displays now include seconds for more precise information
- **Command Enhancements** - `/playtime add` command now supports seconds parameter (`/playtime add <player> <hours>h [<minutes>m] [<seconds>s]`)

#### 📝 Configuration
- **Reduced Console Spam** - Most INFO logs now only appear in debug mode
- **Better Error Handling** - Improved error messages and warnings for configuration issues
- **Language Settings** - Added `language` option in config.yml
- **Lore Placeholder Support** - Added support for PlaceholderAPI placeholders in reward item lore

#### 📋 Commands
- **Tab Completion** - Added tab completion support for all commands
- **Permission Checks** - Enhanced permission validation for admin commands
- **Seconds Support** - Added seconds parameter support to `/playtime add` command

#### 🎯 Logging
- **Conditional Logging** - Most verbose logs now only appear when debug mode is enabled
- **Cleaner Console** - Reduced unnecessary INFO messages in normal operation
- **Debug Mode** - Detailed logging available for troubleshooting configuration issues

#### 🧩 Placeholders
- **Comprehensive Documentation** - Added detailed section in README for all PlaceholderAPI placeholders
- **Advanced Reward Placeholders** - Documented placeholders for reward status tracking
- **Leaderboard Placeholders** - Added new leaderboard placeholders for top players and player positions
- **Usage Examples** - Added practical examples for using placeholders in various contexts

### 🐛 Bug Fixes
- **Configuration Validation** - Fixed potential NullPointerExceptions in configuration loading
- **Reward Processing** - Improved error handling when loading rewards
- **Language Loading** - Fixed issues with loading translation files
- **Placeholder Logic** - Improved logic for determining next reward in progression

### 📖 Documentation
- **README Updates** - Comprehensive documentation of new features
- **Command List** - Added new admin commands to command table
- **Configuration Guide** - Updated configuration documentation with language settings
- **Installation Guide** - Improved installation instructions
- **PlaceholderAPI Support** - Added comprehensive section with all placeholders and usage examples
- **Leaderboard Documentation** - Added documentation for new leaderboard placeholders

### 📦 For Server Owners

#### ✨ New Commands
```
/playtime reload              - Reload plugin configuration
/playtime debug [true/false]  - Toggle debug mode
```

#### ⚙️ Configuration Changes
```yaml
# New debug setting
debug: false

# New language setting
language: en  # Supports: en, zh, pt_BR, ru, fil, de, fr, fi
```

#### 🌍 Language Customization
1. After first run, language files are copied to `plugins/PlayTimePulse/lang/`
2. Edit the file for your selected language
3. Use `/playtime reload` to apply changes without restarting

#### 🐛 Debug Mode
Enable detailed logging for troubleshooting:
```yaml
debug: true
```

Or use the command:
```
/playtime debug true    # Enable debug mode
/playtime debug false   # Disable debug mode
/playtime debug         # Check current debug mode status
```

#### 🧩 PlaceholderAPI Placeholders

**Basic Placeholders**:
- `%playtime_overall_formatted%` - Total formatted playtime
- `%playtime_days%`, `%playtime_hours%`, etc. - Time components

**Advanced Reward Placeholders**:
- `%playtime_required%` - Time for next unclaimed reward
- `%playtime_required_{rewardId}%` - Status of specific reward
- `%playtime_time_left_{rewardId}%` - Time left for specific reward
- `%playtime_claimable_rewards%` - Number of claimable rewards

**Leaderboard Placeholders**:
- `%playtime_top_1%` through `%playtime_top_10%` - Top players in playtime
- `%playtime_leaderboard_position%` - Player's position in leaderboard

See README for complete list and usage examples.

---

## 📈 Previous Versions

*For changelog of previous versions, see GitHub releases.*