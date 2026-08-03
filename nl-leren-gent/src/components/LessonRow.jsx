import { useProgress } from '../context/ProgressContext.jsx'

const TYPE_LABELS = {
  phonetics: { label: 'Uitspraak', icon: '🔉' },
  vocab: { label: 'Woordenschat', icon: '📇' },
  phrases: { label: 'Zinnen', icon: '💬' },
  numbers: { label: 'Getallen', icon: '🔢' },
  grammar: { label: 'Grammatica', icon: '📐' },
  speaking: { label: 'Spreken', icon: '🎙️' },
  quiz: { label: 'Oefening', icon: '✅' },
}

/**
 * Eén les in de lijst. In Stap 1 opent dit (nog) geen oefening; de
 * afvinkknop laat je de voortgang alvast testen. Stap 2 vervangt de
 * onClick door de echte oefen-component.
 */
export default function LessonRow({ lesson, index, onOpen }) {
  const { isDone, toggle } = useProgress()
  const done = isDone(lesson.id)
  const meta = TYPE_LABELS[lesson.type] ?? { label: lesson.type, icon: '•' }

  return (
    <li className="flex items-center gap-3 py-2.5">
      <button
        onClick={() => toggle(lesson.id)}
        aria-pressed={done}
        aria-label={done ? 'Markeer als niet afgerond' : 'Markeer als afgerond'}
        className={`grid h-7 w-7 shrink-0 place-items-center rounded-full border-2 text-xs font-bold transition ${
          done
            ? 'border-emerald-500 bg-emerald-500 text-white'
            : 'border-slate-300 bg-white text-slate-400 hover:border-slate-400'
        }`}
      >
        {done ? '✓' : index}
      </button>

      <button
        onClick={() => onOpen?.(lesson)}
        className="flex min-w-0 flex-1 items-center gap-3 rounded-lg px-1 py-1 text-left hover:bg-slate-50"
      >
        <div className="min-w-0 flex-1">
          <p
            className={`truncate text-sm font-semibold ${
              done ? 'text-slate-400 line-through' : 'text-slate-800'
            }`}
          >
            {lesson.title}
          </p>
          <p className="truncate text-xs text-slate-400">
            {meta.icon} {meta.label} · {lesson.items.length} items
          </p>
        </div>
      </button>
    </li>
  )
}
