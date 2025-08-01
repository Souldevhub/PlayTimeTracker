# 📜 PlayTimePulse Changelog

## 🚀 Version 1.0.4-SNAPSHOT

### 🌟 New Features

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

#### 📝 Configuration
- **Reduced Console Spam** - Most INFO logs now only appear in debug mode
- **Better Error Handling** - Improved error messages and warnings for configuration issues
- **Language Settings** - Added `language` option in config.yml

#### 📋 Commands
- **Tab Completion** - Added tab completion support for all commands
- **Permission Checks** - Enhanced permission validation for admin commands

#### 🎯 Logging
- **Conditional Logging** - Most verbose logs now only appear when debug mode is enabled
- **Cleaner Console** - Reduced unnecessary INFO messages in normal operation
- **Debug Mode** - Detailed logging available for troubleshooting configuration issues

#### 🧩 Placeholders
- **Comprehensive Documentation** - Added detailed section in README for all PlaceholderAPI placeholders
- **Advanced Reward Placeholders** - Documented placeholders for reward status tracking
- **Usage Examples** - Added practical examples for using placeholders in various contexts

### 🐛 Bug Fixes
- **Configuration Validation** - Fixed potential NullPointerExceptions in configuration loading
- **Reward Processing** - Improved error handling when loading rewards
- **Language Loading** - Fixed issues with loading translation files

### 📖 Documentation
- **README Updates** - Comprehensive documentation of new features
- **Command List** - Added new admin commands to command table
- **Configuration Guide** - Updated configuration documentation with language settings
- **Installation Guide** - Improved installation instructions
- **PlaceholderAPI Support** - Added comprehensive section with all placeholders and usage examples

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

See README for complete list and usage examples.

---

## 📈 Previous Versions

*For changelog of previous versions, see GitHub releases.*