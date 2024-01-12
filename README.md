# MiningSpeed
 Example of a custom mining speed on Spigot 1.20.4

Dependencies:
- ProtocolLib

Uses packets (Block break progress packets, and packets to detect mining) with ProtocolLib.

Should work with future(or past?) versions of Minecraft with (minimal) code changes
<!-- This could be done better by listening for a Player Action packet, and listening for the block dig start, end and cancel, instead of using an arm animation event -->
