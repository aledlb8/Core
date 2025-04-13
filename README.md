# Core Plugin

A comprehensive Bukkit/Spigot plugin that provides essential server management and player features.

## Features

### Player Management
- **Player Data Tracking**
  - Tracks player join/quit history
  - Stores player locations
  - Maintains chat history
  - Records player statistics
  - Manages inventory backups

- **Player Commands**
  - `/chathistory` - View a player's chat history
  - `/inventorybackup` - Manage player inventory backups
  - `/location` - View player location history
  - `/playerstats` - View player statistics

### Moderation Tools
- **Ban System**
  - `/tempban` - Temporarily ban players with configurable duration
  - `/unban` - Remove temporary bans
  - Automatic unban after duration expires
  - Ban reason tracking
  - Ban staff tracking

- **Warning System**
  - `/warn` - Issue warnings to players
  - `/unwarn` - Remove warnings from players
  - Configurable warning thresholds
  - Automatic actions at warning thresholds (kick, tempban, ban)
  - Warning history tracking

- **Chat Filter**
  - `/chatfilter` - Manage chat filter rules
  - Configurable filter patterns
  - Multiple action types (block, warn, mute)
  - Custom filter messages
  - Filter rule reloading

- **Player Control**
  - `/freeze` - Freeze players in place
  - `/mute` - Mute players from chatting
  - `/unmute` - Unmute players
  - `/vanish` - Toggle player invisibility

### Staff Tools
- **GameMode Management**
  - `/gmc` - Creative mode
  - `/gms` - Survival mode
  - `/gma` - Adventure mode
  - `/gmsp` - Spectator mode

- **Player Modification**
  - `/feed` - Restore player hunger
  - `/heal` - Heal player health
  - `/fly` - Toggle flight mode
  - `/broadcast` - Send server-wide messages

- **Item Management**
  - `/rename` - Rename items
  - `/addlore` - Add lore to items
  - `/removelore` - Remove lore from items
  - `/more` - Stack items to maximum size

### Kit System
- **Kit Management**
  - `/kit` - Access and manage kits
  - Create custom kits
  - Set kit cooldowns
  - Kit preview system
  - Kit permissions

### Enchantment System
- **Enchantment GUI**
  - `/enchantgui` - Open enchantment interface
  - Visual enchantment selection
  - Level selection
  - Enchantment preview
  - Cost management

### Server Management
- **MOTD System**
  - Customizable server MOTD
  - Server icon support
  - Dynamic MOTD updates
  - MOTD reloading

- **Permission System**
  - Group-based permissions
  - User-specific permissions
  - Permission inheritance
  - Dynamic permission updates
  - Permission reloading

### Economy Integration
- Vault economy support
- Economy commands
- Transaction logging
- Balance tracking

### Utility Features
- **Keep Inventory**
  - `/keepinventory` - Toggle keep inventory
  - Per-world configuration
  - Permission-based access

- **Update Checker**
  - Automatic version checking
  - Update notifications
  - Changelog display

### Configuration
- Extensive configuration options
- Per-feature settings
- Reloadable configurations
- Default value fallbacks

## Permissions

### Core Permissions
- `core.*` - Access to all features
- `core.reload` - Reload plugin configuration
- `core.update` - Check for updates

### Moderation Permissions
- `core.tempban` - Use temporary ban command
- `core.unban` - Use unban command
- `core.warn` - Use warn command
- `core.unwarn` - Use unwarn command
- `core.chatfilter` - Manage chat filters
- `core.freeze` - Use freeze command
- `core.mute` - Use mute command
- `core.vanish` - Use vanish command

### Staff Permissions
- `core.gamemode.*` - Access to all gamemode commands
- `core.feed` - Use feed command
- `core.heal` - Use heal command
- `core.fly` - Use fly command
- `core.broadcast` - Use broadcast command
- `core.rename` - Use rename command
- `core.addlore` - Use addlore command
- `core.removelore` - Use removelore command
- `core.more` - Use more command

### Kit Permissions
- `core.kit.*` - Access to all kits
- `core.kit.<kitname>` - Access to specific kit

### Player Permissions
- `core.chathistory` - Use chathistory command
- `core.inventorybackup` - Use inventorybackup command
- `core.location` - Use location command
- `core.playerstats` - Use playerstats command

## Configuration

The plugin uses multiple configuration files:
- `config.yml` - Main configuration file
- `kits.yml` - Kit configurations
- `warnings.yml` - Warning system data
- `chatfilter.yml` - Chat filter rules
- `playerdata.yml` - Player data storage

## Dependencies
- Vault (for economy features)
- PlaceholderAPI (for placeholder support)

## Requirements
- Java 8 or higher
- Spigot/Paper 1.8.8 or higher
- Vault (optional, for economy features)
- PlaceholderAPI (optional, for placeholder support) 