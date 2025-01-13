#import "@preview/fletcher:0.5.3" as fletcher: diagram, node, edge
#import "@preview/chronos:0.2.0"
#import "@preview/fletcher:0.5.3" as fletcher: diagram, node, edge
#import "./template.typ": *
#show: project.with(
  subject: "Projet Données Réparties",
  title: "Rapport",
  authors: (
    "THEVENET Louis",
    "LEBOBE Timothé",
  ),
  date: "Janvier 2024",
  subtitle: "Groupe L34",
  toc: true,
)
#set text(10pt)

= Fonctionnement
Le `Diary` est permet de lier les fichiers disponibles aux hôtes les proposant au téléchargement.

Le `Daemon` est permet de proposer au téléchargement des fichiers.

Le `Downloader` est permet de lister et télécharger des fichiers.

== RMI

La figure @fig1 représente les fonctions proposées via RMI et leur client.

#figure(caption: [Diagramme RMI ($-> = "fournit" dots " à"$)])[#let da = (0, 0)
  #let di = (0, 1)
  #let do = (1, 0)
  #diagram(
    node-stroke: .1em,
    node-fill: gradient.radial(
      blue.lighten(80%),
      blue,
      center: (30%, 20%),
      radius: 80%,
    ),
    node(di, `Diary`, radius: 2em),
    node(da, `Deamon`, radius: 2em),
    node(do, `Downloader`, radius: 3em),
    edge(di, da, `notifyAlive(), register(), disconnect()`, "-|>", bend: 40deg),
    edge(di, do, `listFiles(), request(), sizeOf()`, "-|>", bend: -40deg),
    edge(da, do, `download()`, "-|>", bend: -40deg),
  )
] <fig1>

La figure @chrono représente un scénario d'utilisation d'Hagimule. Deux Daemons sont connectés au Diary, un Downloader liste les fichiers, recupérère la taille de l'un deux puis le télécharge.
#figure(caption: "Chronogramme du téléchargement d'un fichier")[

  #chronos.diagram({
    import chronos: *
    let da1 = "Daemon 1"
    let da2 = "Daemon 2"
    let di = "Diary"
    let do = "Downloader"
    let do1 = "Download Thread 1"
    let do2 = "Download Thread 2"

    _par(do)
    _par(do1)
    _par(do2)
    _par(di)
    _par(da1)
    _par(da2)
    _seq(da1, di, comment: [`register()`])
    _seq(da1, di, comment: [`notifyAlive()`])
    _seq(da2, di, comment: [`register()`])
    _seq(da2, di, comment: [`notifyAlive()`])
    _seq(do, di, comment: [`listFiles()`])
    _seq(do, di, comment: [`sizeOf()`])
    _seq(do, di, comment: [`request()`])
    _seq(do, da1, comment: [`download()`])
    _seq(do, da2, comment: [`download()`])

    _seq(
      da1,
      do1,
      comment: [_data from Daemon1_],
      dashed: true,
      color: blue,
      create-dst: true,
    )
    _seq(
      da2,
      do2,
      comment: [_data from Daemon2_],
      dashed: true,
      color: red,
      create-dst: true,
    )

    _seq(da1, do1, dashed: true, color: blue)
    _seq(da2, do2, dashed: true, color: red)
    _seq(da1, do1, dashed: true, color: blue, destroy-dst: true)
    _seq(da2, do2, dashed: true, color: red, destroy-dst: true)

    _seq(da1, di, comment: [`notifyAlive()`])
    _seq(da2, di, comment: [`notifyAlive()`])
    _seq(da1, di, comment: [`disconnect()`])
    _seq(da2, di, comment: [`disconnect()`])
  })
] <chrono>




= Blog

== Rapport du 13/12/2024:

Nous avons une première version fonctionnelle :
- Les Daemon peuvent s'enregistrer auprès du Diary (Fait par Timothé)
- Les Downloader peuvent lister les fichiers, obtenir un hôte qui les propose et les télécharger depuis celui-ci (Fait par Louis)

Pour le moment nous avons seulement fait le téléchargement depuis un hôte.
Nous rencontrons des difficultés à faire fonctionner notre projet sur plusieurs machines:
- Entre différentes machines de l'n7, tout fonctionne
- Entre des machines de notre réseau privée, ça ne fonctionne pas
- Entre nos machines et celles de l'n7 (en VPN ou direct), ça fonctionne des fois...


== Rapport du 18/12/2024

Nous sommes en train d'implémenter le téléchargement en parallèle.

Nous rencontrons des difficultés lors de la création des tâches à distribuer aux threads, notamment la découpe du fichier en plusieurs morceaux.

== Rapport du 23/12/2024
Nous avons terminé le téléchargement parallèle ensemble. Mais il n'était pas aussi efficace que prévu car après avoir récolté tous les fragments, on écrit le fichier sur le disque. Le temps de cette écriture est proportionnelle à la taille du fichier, elle consomme aussi beaucoup de mémoire vive pour de gros fichiers.

== Rapport du 05/01
Louis a remplacé l'écriture par une écriture en parallèle via des `FileChannel` pour écrire au fur et à mesure que l'on télécharge. En divisant correctement la charge, on s'assure que les threads ne rentrent pas en collision sur le fichier partagé.

Les `FileChannel` nous ont fait passer de 29s à 12s pour un fichier de 1Gb téléchargé depuis 4 démons (tous sur des machines de l'N7).

#let res_130M_1_10MS = json("./results_130M_1_10ms.json").at("results").at(0);
#let res_130M_4_10MS = json("./results_130M_4_10ms.json").at("results").at(0);
#let res_130M_8_10MS = json("./results_130M_8_10ms.json").at("results").at(0);

#table(
  columns: 4,
  [Démons], [Temps (s)], [Min (s)], [Max (s)],
  [$1$],
  [$#calc.round(res_130M_1_10MS.at("mean"), digits:2) plus.minus #calc.round(res_130M_1_10MS.at("stddev"), digits:2)$],
  [$#calc.round(res_130M_1_10MS.at("min"), digits:2)$],
  [$#calc.round(res_130M_1_10MS.at("max"), digits:2)$],

  [$4$],
  [$#calc.round(res_130M_4_10MS.at("mean"), digits:2) plus.minus #calc.round(res_130M_4_10MS.at("stddev"), digits:2)$],
  [$#calc.round(res_130M_4_10MS.at("min"), digits:2)$],
  [$#calc.round(res_130M_4_10MS.at("max"), digits:2)$],

  [$8$],
  [$#calc.round(res_130M_8_10MS.at("mean"), digits:2) plus.minus #calc.round(res_130M_8_10MS.at("stddev"), digits:2)$],
  [$#calc.round(res_130M_8_10MS.at("min"), digits:2)$],
  [$#calc.round(res_130M_8_10MS.at("max"), digits:2)$],
)

== Rapport du 10/01
Timothé a implémenté la gestion des daemons déconnectés. Les daemons envoient des _battements de coeur_ toutes les 80s, si le Diary ne reçoit rien pendant 85s, le daemons est retiré des hôtes enregistrés.

#figure(
  caption: "Temps de téléchargment en fonction du nombre de démons avec 10ms de délai tous les 64Ko (10 itérations par point)",
)[
  #image("./plot_130M_10ms.svg", height: 30%)
]
