# AziSwitchVelocity
Switch between member and admin groups.

Legacy spigot plugin: [AziSwitch](https://github.com/AzisabaNetwork/AziSwitch)

## Usage
- `/newswitchgroup <group>` to create new groups for switching with /switch
- `/switch [groups]` to switch between member and admin groups.
- `/aziswitchreload` to reload the config.yml.

## Permissions
- `aziswitch.aziswitchreload`
- `aziswitch.newswitchgroup`

## Configuration
Edit the config.yml to change the settings of AziSwitchVelocity.
```yml
# server=<server_name>
contextualGroups:
- admin
- moderator
- builder

# server=global
nonContextualGroups:
- owner
- developer
- alladmin

servers:
  life: life
  lifepve: life
```
