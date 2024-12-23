= Rapport du 13/12/2024:

Nous avons une première version fonctionnelle :
- Les Daemon peuvent s'enregistrer auprès du Diary
- Les Downloader peuvent lister les fichiers, obtenir les hôtes qui les proposent et les télécharger

Pour le moment nous avons seulement fait le téléchargement depuis un hôte.
Nous rencontrons des difficultés à faire fonctionner notre projet sur plusieurs machines:
- Entre différentes machines de l'n7, tout fonctionne
- Entre des machines de notre réseau privée, ça ne marche pas
- Entre nos machines et celles de l'n7 (en VPN ou direct), ça fonctionne des fois


= Rapport du 18/12/2024

Nous sommes en train d'implémenter le téléchargement en parallèle. Nous n'avons pas encore exactement compris pourquoi le projet ne fonctionne pas dans un réseau domestique.

= Rapport du 19/12/2024
Le téléchargement parallèle est terminé. Au début il n'était pas si efficace que ça car l'écriture dans le fichier nous limitait. On a décidé d'utiliser des `FileChannel` pour écrire au fur et à mesure que l'on télécharge.

Les `FileChannel` nous ont fait passer de 29s à 12s pour un fichier de 1Gb téléchargé depuis 4 démons (tous sur des machines de l'N7).
