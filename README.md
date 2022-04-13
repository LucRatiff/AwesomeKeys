# AwesomeKeys

AwesomeKeys is a Bukkit plugin which allows you to add **Locks** and **Keys** to your Minecraft server.

Any door, fence gate or container can be locked with a normal lock (which requires a key), or a 9 digit code. Any item can be defined as a key in the config file : this plugin is fully and deeply customizable.

You can also add descriptions to your keys to find them easily, and add greeting messages when a door is unlocked.
Cherry on the top, you have nice particles for each action.

## Which blocks can be locked ?

All doors, fence gates, shulkers, chests and all other containers (except hoppers and entities). You can customize the list in the configuration file.

## Which item can be a key ?

Any item. By default, the key is the wooden stick. When the item is affected as a key, there are special tags applied to it. You can define mutliple items and durabilities as keys. You may prefer an item that players cannot obtain, and maybe an item on wich you can add a new texture : for more information on how to customize key items, please refer to the documentation.

## How to enter a code ?

If the block is locked with a code, it will display an 3x3 inventory on access. Just click on the numbered wool blocks.

## How to use the plugin in game

The main command is /key, or the alias /k (you can customize the alias). First, you have to define a new Lock with a name. You can define as many locks as you wish. Write the name somewhere, because you will need it to create new keys. There is no name conflict with other players, each lock also has a unique identifier (uuid).

Defining a new lock :
/k new lock <name>

Then, you (and only you) can create new keys related to this lock :
/k new key <name>

Use the same name for the lock and its keys. You can then share the keys to your friends (or sell them or whatever you want). Be careful to whom you give your keys, because in the latter you won't really know who will access your locked blocks.

You or the player that received your key can add a description in a maximum of four lines :
/k editkey <line> <description>

You can then add the lock you created on any lockable block :
/k add <name>

The block your are pointing at will get locked. If you don't want it anymore, you can remove the lock :
/k remove <name>

If the locked block is a door or a fence gate, you can add a greeting message, with the color of your choice (a single Minecraft color code character among the following : 0123456789abcdef) :
/k greeting <color code> <greeting message>

To remove the greeting message :
/k greeting none

Note that a lock doesn't protect the block from destruction if it's not protected by a plugin.

## The Master Key

For server owners and staff members, there is a special key wich can access any locked block.
/k master

## Logs

There are logs for any action related to locks and keys. If you have the permission, you can print the logs
in the chat or in a file. The logs can be filtered with many tags. Please refer to the documentation to use
it properly.
/k logs <filters> (> <file>)

## Permissions

awesomekeys.lock.create : all
awesomekeys.lock.purge : op
awesomekeys.master : op
awesomekeys.logs.read : op
awesomekeys.logs.print : op
awesomekeys.verifylang : op

## Language support

All text displayed in game is written in a language file. You can rely on the default (lang\_en.json) to make your own for the language used on your server. The file is very easy to customize, as you can see in the documentation. If you want to submit your own, please check first if it does exist on the repository. Then please respect the naming convention : "lang\_" + <language code (de, it, es, ru, jp, cn...)> + ".json". If you see bad translations, please submit corrections.

The language file contains a "version" field, which changes if an update adds or removes entries. If a language file is outdated, the missing entries will be replaced by the default. Chances are not many entries should be added. In game, you can verify how the messages are displayed with /akverifylang <lang> (<page>)

Note : there is **no warranty** that the language file will be correct. Please check first by comparing with the default lang\_en.json. A list of verified translations will be added to the documentation to help you. I personnally provide the french language (lang\_fr.json).

## TODO list for future updates :
- API for the developpers
