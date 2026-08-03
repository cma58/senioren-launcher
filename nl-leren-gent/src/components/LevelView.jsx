import LessonRow from './LessonRow.jsx'
import ProgressBar from './ProgressBar.jsx'
import { allLessonIds } from '../data/curriculum.js'
import { useProgress } from '../context/ProgressContext.jsx'

/**
 * Detailweergave van één niveau: alle modules met hun lessen.
 * Elke module toont zijn eigen mini-voortgang.
 */
export default function LevelView({ level, onOpenLesson }) {
  const { ratioFor } = useProgress()
  const levelRatio = ratioFor(allLessonIds(level))

  return (
    <div className="mx-auto max-w-2xl px-4 py-6">
      {/* Kop van het niveau */}
      <div className="mb-6 flex items-center gap-4">
        <div className="grid h-14 w-14 shrink-0 place-items-center rounded-2xl bg-slate-100 text-3xl">
          {level.icon}
        </div>
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-slate-900">{level.title}</h2>
            {level.cefr && (
              <span className="rounded-full bg-slate-200 px-2 py-0.5 text-xs font-semibold text-slate-700">
                {level.cefr}
              </span>
            )}
          </div>
          <p className="text-sm text-slate-500">{level.subtitle}</p>
        </div>
      </div>

      <div className="mb-8">
        <ProgressBar ratio={levelRatio} accent={level.accent} />
      </div>

      {/* Modules */}
      <div className="space-y-4">
        {level.modules.map((module) => {
          const moduleRatio = ratioFor(module.lessons.map((l) => l.id))
          return (
            <section key={module.id} className="card p-5">
              <div className="mb-1 flex items-start gap-3">
                <span className="text-2xl" aria-hidden="true">
                  {module.icon}
                </span>
                <div className="min-w-0 flex-1">
                  <div className="flex items-baseline gap-2">
                    <span className="text-xs font-bold text-slate-400">Module {module.id}</span>
                  </div>
                  <h3 className="text-base font-bold text-slate-900">{module.title}</h3>
                  <p className="text-sm text-slate-500">{module.goal}</p>
                </div>
              </div>

              <div className="my-3">
                <ProgressBar ratio={moduleRatio} accent={level.accent} showLabel={false} />
              </div>

              <ul className="divide-y divide-slate-100">
                {module.lessons.map((lesson, i) => (
                  <LessonRow
                    key={lesson.id}
                    lesson={lesson}
                    index={i + 1}
                    onOpen={onOpenLesson}
                  />
                ))}
              </ul>
            </section>
          )
        })}
      </div>
    </div>
  )
}
