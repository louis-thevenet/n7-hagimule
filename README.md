# hagimule

## Archi

- Un serveur annuaire implémentation HashMap<String, Tuple Class Int \* List<Host>>
- Un client possede des fichiers qui publient sur l'annuaire
  - Il peut aussi télécharger d'autre fichier sur disponible sur le serveur annuaire.
    Le téléchargement doitêtre découpé en plusieurs morceau de ton fichier.

## Améliorations visées

Le client peut vérifier l'authenticité
du fichier grace au checksum donnée avec la liste donnée par l'annuaire des hosts qui le possèdent.
