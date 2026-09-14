# Ton rendez-vous forme

Application Android personnelle : garder une habitude quotidienne, gagner progressivement en force, souffle, équilibre et mobilité. Matériel : une haltère réglable jusqu’à 13 kg, une bande longue, une chaise stable et un tapis.

## Parcours

Le cycle suit les séances complètes : **Force A → mobilité → Force B → cardio → Force A → mobilité → cardio**. Pas de rattrapage ni de progression liée aux jours écoulés. Après du renforcement la veille, ou une séance déjà terminée aujourd’hui, une journée douce peut remplacer la proposition sans consommer la prochaine séance. Le bouton journée douce est toujours disponible.

- Force A : squat avec haltère, pompes, tirage haltère gauche/droite, soulevé de terre avec une haltère tenue à deux mains, dead bug.
- Force B : fentes arrière alternées, développé au sol gauche/droite, tirage bande sous les pieds, gainage latéral gauche/droite.
- Cardio : marche active, pas latéraux, boxe sans charge, squats, équilibre alterné.
- Mobilité : épaules, chat-vache, rotation assise, chevilles, équilibre.

Chaque séance comporte 10 s d’installation, 2 min de mise en mouvement, deux passages, puis 1 min de retour au calme. Force : 35/15 s, puis 40/15 s et 45/15 s, soit **13:10, 14:10, 15:10**. Cardio et mobilité : **13:10**, à une intensité confortable. Ces durées incluent les transitions ; les pauses volontaires s’ajoutent. Prolonger l’échauffement ou la récupération si nécessaire.

Trois séances de force complètes « Facile » à un même palier, sur trois dates différentes, augmentent le palier. « Difficile » le réduit. Une interruption d’au moins sept jours depuis la dernière force réduit le palier proposé. La charge n’augmente jamais automatiquement : viser des répétitions contrôlées avec 2–3 répétitions possibles en réserve. Une fois le palier 3 confortable, augmenter doucement les répétitions ou la charge sur un mouvement à la fois. 13 kg est une capacité du matériel, pas une prescription.

Les créneaux d’effort incluent les petites pauses nécessaires : ils ne mesurent pas les mouvements réellement réalisés. Pour les exercices nouveaux, lire les indications avant de démarrer ; ne pas utiliser le GIF d’un autre mouvement comme démonstration. Les exercices nouveaux ont des consignes écrites et une option douce. La marche et les déplacements actifs complètent ces courtes séances pour la santé à long terme.

## Fiabilité

- Chronomètre monotone, temps de pause exclu, durée réalisée et créneaux d’exercice séparés.
- Passage manuel de la dernière étape traité comme une fin ; exercices sautés ou arrêt anticipé enregistrés comme partiels, sans progression.
- Enregistrement Room avant de quitter ; erreurs visibles et nouvelle tentative sans doublon.
- Pause automatique quand l’app perd le premier plan ; reprise manuelle. État conservé avec SavedStateHandle en cas de recréation Android (hors arrêt forcé ou suppression de la tâche).
- Migration **4 → 5** conservant historique et playlist. Pas de migration destructive. Les anciennes durées restent inchangées car leur durée réelle ne peut pas être reconstituée ; elles n’alimentent pas la nouvelle progression.
- Audio préparé de façon asynchrone, nombre de tentatives limité, piste intégrée en l’absence de playlist personnelle.

## Vérification

`gradlew testDebugUnitTest assembleDebug lintDebug`

Avec un émulateur : `gradlew connectedDebugAndroidTest`. Tests du moteur, du programme, de la migration 4→5 et du parcours démarrer/pause/passer/enregistrer.

Conserver le même identifiant d’application et la même clé de signature pour installer par-dessus l’ancienne version. Une APK de développement compilée sur une autre machine peut avoir une clé différente : ne pas désinstaller l’ancienne app pour contourner cela, sous peine de perdre son historique.

## Repères utilisés

Le programme est un choix pratique pour cet usage personnel, pas un protocole validé de longévité. Les recommandations publiques soutiennent la régularité, le renforcement progressif et l’activité aérobie en complément :

- [OMS — activité physique](https://www.who.int/news-room/fact-sheets/detail/physical-activity)
- [NHS — renforcer progressivement sa force et sa souplesse](https://www.nhs.uk/live-well/exercise/how-to-improve-strength-flexibility/)
- [CDC — intensité et test de la parole](https://www.cdc.gov/physical-activity-basics/adding-adults/what-counts.html)
