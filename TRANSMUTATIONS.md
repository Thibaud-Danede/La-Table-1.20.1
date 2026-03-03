# Référence des transmutations — Table d'alchimie

Document de suivi des blocs et items transmutables dans la table d'alchimie.

---

## Catalyseurs utilisables

Toutes les recettes utilisent le tag `**tablemod:alchemy_catalyst**`.

**Fichier à éditer pour modifier les catalyseurs :**

```
src/main/resources/data/tablemod/tags/items/alchemy_catalyst.json
```

**Catalyseurs actuels :**


| Item           |
| -------------- |
| Glowstone Dust |
| Diamond        |
| Bone Meal      |
| Echo Shard     |
| Amethyst Shard |


---

## Arborescence des recettes

```
src/main/resources/data/tablemod/recipes/alchemy/
├── colored/           # Items colorés (laine, verre, terracotta, etc.)
│   ├── banner.json
│   ├── bed.json
│   ├── carpet.json
│   ├── concrete.json
│   ├── concrete_powder.json
│   ├── glass.json
│   ├── glass_panes.json
│   ├── shulker_box.json
│   ├── terracotta.json
│   └── wool.json
├── nature/            # Éléments naturels
│   └── sapling.json
├── nether/            # Blocs du Nether
│   └── soul_soil.json
├── stone/             # Pierre et dérivés
│   ├── stone.json
│   ├── stone_bricks.json
│   └── walls.json
└── wood/              # Bois
    ├── fence.json
    ├── logs.json
    ├── planks.json
    └── stripped_logs.json
```

---

## Liste des transmutations

### Colored (items colorés)


| Transmutation       | Ingrédient (tag)           | Résultats                              | Fichier                                |
| ------------------- | -------------------------- | -------------------------------------- | -------------------------------------- |
| **Bannières**       | `forge:banners`            | Toutes les bannières colorées          | `alchemy/colored/banner.json`          |
| **Lits**            | `forge:beds`               | Tous les lits colorés                  | `alchemy/colored/bed.json`             |
| **Tapis**           | `forge:wool_carpets`       | Tous les tapis colorés                 | `alchemy/colored/carpet.json`          |
| **Béton**           | `tablemod:concrete`        | 16 couleurs de béton                   | `alchemy/colored/concrete.json`        |
| **Poudre de béton** | `tablemod:concrete_powder` | 16 couleurs de poudre                  | `alchemy/colored/concrete_powder.json` |
| **Verre**           | `forge:glass`              | Verre, verre teinté, verre coloré (16) | `alchemy/colored/glass.json`           |
| **Vitres**          | `forge:glass_panes`        | Vitre, vitres colorées (16)            | `alchemy/colored/glass_panes.json`     |
| **Shulker boxes**   | `forge:shulker_boxes`      | Toutes les shulker boxes colorées      | `alchemy/colored/shulker_box.json`     |
| **Terracotta**      | `forge:terracotta`         | Toutes les terracotta colorées         | `alchemy/colored/terracotta.json`      |
| **Laine**           | `forge:wool`               | Toutes les laines colorées             | `alchemy/colored/wool.json`            |


### Stone (pierre)


| Transmutation         | Ingrédient (tag)     | Résultats                                                                           | Fichier                           |
| --------------------- | -------------------- | ----------------------------------------------------------------------------------- | --------------------------------- |
| **Pierre**            | `forge:stone`        | Stone, granite, diorite, andesite, cobblestone, smooth_stone, deepslate, tuff, etc. | `alchemy/stone/stone.json`        |
| **Briques de pierre** | `forge:stone_bricks` | Stone bricks, mossy, cracked, chiseled                                              | `alchemy/stone/stone_bricks.json` |
| **Murs (barrières)**  | `forge:walls`        | Cobblestone, brick, stone brick, deepslate, nether brick, etc.                     | `alchemy/stone/walls.json`        |


### Wood (bois)


| Transmutation | Ingrédient (tag) | Résultats                            | Fichier                    |
| ------------- | ---------------- | ------------------------------------ | -------------------------- |
| **Barrières en bois** | `forge:fences` | Toutes les barrières en bois + nether brick | `alchemy/wood/fence.json` |
| **Bois (bûches)** | `forge:logs` | Toutes les bûches et bois (logs, wood, stems, hyphae) | `alchemy/wood/logs.json` |
| **Planches**  | `forge:planks`   | Toutes les planches (vanilla + mods) | `alchemy/wood/planks.json` |
| **Bois écorcé** | `forge:stripped_logs` | Bûches et bois écorcés (stripped logs, stripped wood) | `alchemy/wood/stripped_logs.json` |


### Nature


| Transmutation | Ingrédient (tag) | Résultats                   | Fichier                       |
| ------------- | ---------------- | --------------------------- | ----------------------------- |
| **Pousses**   | `forge:saplings` | Toutes les pousses d'arbres | `alchemy/nature/sapling.json` |


### Nether


| Transmutation             | Ingrédient                   | Résultats | Fichier                         |
| ------------------------- | ---------------------------- | --------- | ------------------------------- |
| **Soul Sand → Soul Soil** | `minecraft:soul_sand` (item) | Soul Soil | `alchemy/nether/soul_soil.json` |


---

## Légende des tags


| Type                  | Description                                                                  |
| --------------------- | ---------------------------------------------------------------------------- |
| **forge:**            | Tag Forge — compatibilité mods automatique si le mod ajoute ses items au tag |
| **tablemod:**         | Tag du mod — liste explicite dans `data/tablemod/tags/items/`                |
| **minecraft:** (item) | Item vanilla spécifique                                                      |


---

## Format des recettes

### Avec tag de résultats (recommandé pour compatibilité mods)

```json
{
  "type": "tablemod:alchemy",
  "ingredient": { "tag": "forge:nom_du_tag" },
  "catalyst": { "tag": "tablemod:alchemy_catalyst" },
  "results_tag": "forge:nom_du_tag"
}
```

### Avec liste explicite de résultats

```json
{
  "type": "tablemod:alchemy",
  "ingredient": { "tag": "tablemod:mon_tag" },
  "catalyst": { "tag": "tablemod:alchemy_catalyst" },
  "results": [
    { "item": "minecraft:item_1" },
    { "item": "minecraft:item_2" }
  ]
}
```

---

## Fichiers à éditer — Chemin complet


| Transmutation     | Chemin absolu                                                                   |
| ----------------- | ------------------------------------------------------------------------------- |
| Catalyseurs       | `src/main/resources/data/tablemod/tags/items/alchemy_catalyst.json`             |
| Bannières         | `src/main/resources/data/tablemod/recipes/alchemy/colored/banner.json`          |
| Lits              | `src/main/resources/data/tablemod/recipes/alchemy/colored/bed.json`             |
| Tapis             | `src/main/resources/data/tablemod/recipes/alchemy/colored/carpet.json`          |
| Béton             | `src/main/resources/data/tablemod/recipes/alchemy/colored/concrete.json`        |
| Poudre de béton   | `src/main/resources/data/tablemod/recipes/alchemy/colored/concrete_powder.json` |
| Verre             | `src/main/resources/data/tablemod/recipes/alchemy/colored/glass.json`           |
| Vitres            | `src/main/resources/data/tablemod/recipes/alchemy/colored/glass_panes.json`     |
| Shulker boxes     | `src/main/resources/data/tablemod/recipes/alchemy/colored/shulker_box.json`     |
| Terracotta        | `src/main/resources/data/tablemod/recipes/alchemy/colored/terracotta.json`      |
| Laine             | `src/main/resources/data/tablemod/recipes/alchemy/colored/wool.json`            |
| Pierre            | `src/main/resources/data/tablemod/recipes/alchemy/stone/stone.json`             |
| Briques de pierre | `src/main/resources/data/tablemod/recipes/alchemy/stone/stone_bricks.json`      |
| Murs (barrières)  | `src/main/resources/data/tablemod/recipes/alchemy/stone/walls.json`             |
| Barrières en bois | `src/main/resources/data/tablemod/recipes/alchemy/wood/fence.json`              |
| Bois (bûches)     | `src/main/resources/data/tablemod/recipes/alchemy/wood/logs.json`             |
| Bois écorcé       | `src/main/resources/data/tablemod/recipes/alchemy/wood/stripped_logs.json`     |
| Planches          | `src/main/resources/data/tablemod/recipes/alchemy/wood/planks.json`             |
| Pousses           | `src/main/resources/data/tablemod/recipes/alchemy/nature/sapling.json`          |
| Soul Sand/Soil    | `src/main/resources/data/tablemod/recipes/alchemy/nether/soul_soil.json`        |


---

*Dernière mise à jour : 03/03/2026*