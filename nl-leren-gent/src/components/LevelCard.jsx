import ProgressBar from './ProgressBar.jsx'
import { allLessonIds, countLessons } from '../data/curriculum.js'
import { useProgress } from '../context/ProgressContext.jsx'

/**
 * Kaart voor één niveau op het dashboard.
 */
export default function LevelCard({ level, onOpen }) {
  const { ratioFor, countFor } = useProgress()
  const ids = allLessonIds(level)
  const ratio = ratioFor(ids)
  const done = countFor(ids)
  const total = countLessons(level)

  const ring = level.accent === 'saffraan' ? 'hover:ring-saffraan-300' : 'hover:ring-gent-300'
  const badge =
    level.accent === 'saffraan'
      ? 'bg-saffraan-100 text-saffraan-800'
      : 'bg-gent-100 text-gent-800'

  return (
    <button
      onClick={() => onOpen(level)}
      className={`card group w-full p-5 text-left hover:-translate-y-0.5 hover:shadow-md ${ring} hover:ring-2`}
    >
      <div className="flex items-start gap-4">
        <div className="grid h-12 w-12 shrink-0 place-items-center rounded-xl bg-slate-100 text-2xl">
          {level.icon}
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <h3 className="text-lg font-bold text-slate-900">{level.title}</h3>
            {level.cefr && (
              <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${badge}`}>
                {level.cefr}
              </span>
            )}
          </div>
          <p className="text-sm font-medium text-slate-600">{level.subtitle}</p>
          <p className="mt-1.5 text-sm leading-snug text-slate-500">{level.description}</p>
        </div>
        <span
          className="mt-1 text-slate-300 transition group-hover:translate-x-0.5 group-hover:text-slate-500"
          aria-hidden="true"
        >
          →
        </span>
      </div>

      <div className="mt-4">
        <div className="mb-1.5 flex justify-between text-xs font-medium text-slate-500">
          <span>
            {level.modules.length} modules · {total} lessen
          </span>
          <span>
            {done}/{total} afgerond
          </span>
        </div>
        <ProgressBar ratio={ratio} accent={level.accent} showLabel={false} />
      </div>
    </button>
  )
}
