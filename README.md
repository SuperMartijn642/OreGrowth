![Ore Growth banner](https://imgur.com/lUuE4jX.png)

<div align='center'>

### **Ore Growth** allows crystal clusters to grow on ores! Clusters can then be harvested for resources.
</div>
<br>

![Separator](https://imgur.com/VJlpisR.png)

## Releases

For more info and downloads, check out the project on CurseForge and Modrinth:  

<img alt="modrinth" align="center" height="28" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/modrinth_vector.svg"> - [Ore Growth on Modrinth](https://modrinth.com/mod/ore-growth)  
<img alt="curseforge" align="center" height="28" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy-minimal/available/curseforge_vector.svg"> - [Ore Growth on CurseForge](https://curseforge.com/minecraft/mc-mods/ore-growth)

![Separator](https://imgur.com/cOOwZGx.png)

## Default configuration
By default, crystals can grow on any of the vanilla blocks as well as on ancient debris.

![Separator](https://imgur.com/bDXpr0y.png)

## Custom recipes

Ore Growth is completely customizable and custom recipes can allow crystals to grow on a certain block. Adding new recipes or adjusting default recipes can be done through a datapack.  
To add a recipe, create a new JSON file in the 'recipes' folder and set its type to `oregrowth:ore_growth`. Here is an example of the properties for an ore growth recipes to make crystals grow on pumpkins:

<table>
<tr><td>

```json5
{
   "type": "oregrowth:ore_growth",
   "base": "minecraft:pumpkin", // block the crystal grows on
   "stages": 4, // number of growth stages the crystal has
   "spawn_chance": 0.2, // chance for a crystal to spawn when the
                        // base receives a random tick
   "growth_chance": 0.3, // chance for a crystal to grow when it
                         // receives a random tick
   "result": { // item dropped when a fully grown crystal is broken
      "item": "minecraft:pumpkin_pie",
      "count": 2
   }
}
```

</td><td>
<img width='500' src='https://imgur.com/Sp2j1Yl.png' alt='Crystals growing on a pumpkin'>
</td></tr>
</table>

![Separator](https://imgur.com/VJlpisR.png)

## Just Enough Items integration
All ore growth recipes are visible in recipe viewing mods.

<img width='404' alt='Examples of ore growth recipes in JEI' src='https://imgur.com/eURhlC2.gif'>

![Separator](https://imgur.com/cOOwZGx.png)

## Jade and The One Probe integration
Both Jade and The One Probe will display the growth progress of crystals.

<img width='400' alt='Jade tooltip showing the crystals growth progress' src='https://imgur.com/6M4iXVJ.gif'>

![Separator](https://imgur.com/bDXpr0y.png)

## FAQ
**Isn't this mod very overpowered?**  
Yes, however probably not as much as you might think as the crystals take quite long to grow. For example, diamond ore crystals may take 30 minutes to an hour to fully grow  
**Can I use your mod in my modpack?**  
Yes, feel free to use my mod in your modpack

![Separator](https://imgur.com/VJlpisR.png)

## Discord
For future content, upcoming mods, and discussion, feel free to join the SuperMartijn642 discord server!  
[<img width='400' src='https://imgur.com/IG1us6p.png'>](https://discord.gg/QEbGyUYB2e)

![Separator](https://imgur.com/cOOwZGx.png)
