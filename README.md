# JukeAlert

A Minecraft 1.18.2 plugin that turns Noteblocks and Jukeboxes into 'snitches' that record player entries and actions. Snitches allow players to deal with griefers without the need for admin involvement.

## Usage

- Reinforce a **Jukebox** with a [Citadel](https://github.com/CivMC/Citadel) reinforcement to create a snitch. It will send a notification to everyone on the [NameLayer](https://github.com/CivMC/NameLayer) group its reinforced to when other players enter its field - an 11 block cube centered on the snitch. It will also record player actions that occur within the field, e.g. block placement and destruction. Type `/ja` to check what players did in the snitched area while you were gone.

- Reinforce a **Noteblock** to create an *entry* snitch. It will send player entry notifications just like the Jukebox snitch, but will not record player actions.

See the guide on the community CivMC wiki: https://civwiki.org/wiki/Snitch

## Contributing
- Style guide: https://github.com/DevotedMC/style-guide
- Build server: https://build.devotedmc.com/job/JukeAlert-master/
