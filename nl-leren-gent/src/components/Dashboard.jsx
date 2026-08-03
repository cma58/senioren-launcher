import LevelCard from './LevelCard.jsx'
import ProgressBar from './ProgressBar.jsx'
import curriculum, { allLessonIds } from '../data/curriculum.js'
import { useProgress } from '../context/ProgressContext.jsx'

/**
 * Startscherm: welkomstboodschap, totale voortgang en de niveaukeuze.
 */
export default function Dashboard({ onOpenLevel }) {
  const { ratioFor } = useProgress()
  const allIds = curriculum.levels.flatMap(allLessonIds)
  const overall = ratioFor(allIds)

  return (
    <div className="mx-auto max-w-2xl px-4 py-6">
      {/* Welkom */}
      <section className="card overflow-hidden">
        <div className="bg-gradient-to-br from-gent-700 to-gent-500 p-6 text-white">
          <p className="text-sm font-medium text-gent-100">
            Welkom / <span className="rtl">مرحبا</span>
          </p>
          <h2 className="mt-1 text-2xl font-bold leading-tight">
            Leer stap voor stap Nederlands voor Gent
          </h2>
          <p className="mt-2 max-w-md text-sm text-gent-100">
            Een cursus op maat van Darija-sprekers uit Oujda. Van de eerste klanken
            tot je eerste gesprek bij de bakker.
          </p>
        </div>
        <div className="p-5">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm font-semibold text-slate-700">Jouw totale voortgang</span>
          </div>
          <ProgressBar ratio={overall} />
        </div>
      </section>

      {/* Niveaus */}
      <h3 className="mb-3 mt-8 px-1 text-sm font-bold uppercase tracking-wide text-slate-500">
        Kies een niveau
      </h3>
      <div className="grid gap-4">
        {curriculum.levels
          .slice()
          .sort((a, b) => a.order - b.order)
          .map((level) => (
            <LevelCard key={level.id} level={level} onOpen={onOpenLevel} />
          ))}
      </div>

      <p className="mt-8 text-center text-xs text-slate-400">
        Fase 1 · Dashboard &amp; leerlijn. De interactieve oefeningen (audio, spraak, AI-check)
        volgen in de volgende stap.
      </p>
    </div>
  )
}
