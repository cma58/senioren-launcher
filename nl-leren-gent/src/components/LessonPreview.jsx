import { useEffect } from 'react'
import { isTTSAvailable, speak } from '../lib/speech.js'
import { useProgress } from '../context/ProgressContext.jsx'

/**
 * Voorbeeldpaneel voor een les (onderste blad / bottom sheet).
 *
 * In Stap 1 toont dit de inhoud van de les met een voorleesknop (native TTS),
 * zodat de leerlijn meteen bruikbaar is. De volledige interactieve oefeningen
 * (opnemen met Whisper, AI-check met Gemini) komen in Stap 2 — de opzet is
 * al modulair zodat we per lestype (lesson.type) een component kunnen inpluggen.
 */
export default function LessonPreview({ lesson, onClose }) {
  const { isDone, toggle } = useProgress()

  // Sluiten met Escape.
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  if (!lesson) return null
  const done = isDone(lesson.id)

  return (
    <div className="fixed inset-0 z-40 flex items-end justify-center sm:items-center">
      {/* Achtergrond */}
      <div
        className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Blad */}
      <div className="relative flex max-h-[85vh] w-full max-w-lg flex-col overflow-hidden rounded-t-3xl bg-white shadow-xl sm:rounded-3xl">
        <div className="flex items-start justify-between gap-3 border-b border-slate-100 p-5">
          <div className="min-w-0">
            <p className="text-xs font-bold uppercase tracking-wide text-slate-400">
              Les {lesson.id}
            </p>
            <h3 className="text-lg font-bold text-slate-900">{lesson.title}</h3>
          </div>
          <button onClick={onClose} className="btn-ghost h-9 w-9 !px-0" aria-label="Sluiten">
            ✕
          </button>
        </div>

        <div className="overflow-y-auto p-5">
          <p className="text-sm leading-relaxed text-slate-600">{lesson.intro}</p>
          {lesson.darijaNote && (
            <p className="rtl mt-2 rounded-lg bg-saffraan-50 p-3 text-sm text-saffraan-900">
              {lesson.darijaNote}
            </p>
          )}

          <ul className="mt-4 space-y-2">
            {lesson.items.map((item, i) => (
              <li
                key={i}
                className="flex items-start gap-3 rounded-xl border border-slate-100 p-3"
              >
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
                    {item.article && (
                      <span className="text-xs font-semibold text-gent-600">{item.article}</span>
                    )}
                    <span className="font-semibold text-slate-900">{item.nl}</span>
                    {item.pair && (
                      <span className="text-slate-400">↔ {item.pair}</span>
                    )}
                    {typeof item.value === 'number' && (
                      <span className="rounded bg-slate-100 px-1.5 text-xs font-bold text-slate-500">
                        {item.value}
                      </span>
                    )}
                  </div>
                  {item.ipa && <p className="text-xs text-slate-400">{item.ipa}</p>}
                  {(item.darija || item.darijaLat) && (
                    <p className="text-sm text-slate-500">
                      {item.darija && <span className="rtl">{item.darija}</span>}
                      {item.darija && item.darijaLat && ' · '}
                      {item.darijaLat && <span className="italic">{item.darijaLat}</span>}
                    </p>
                  )}
                  {item.tip && <p className="mt-0.5 text-xs text-emerald-700">💡 {item.tip}</p>}
                  {item.example && (
                    <p className="mt-0.5 text-xs text-slate-400">bv. {item.example}</p>
                  )}
                </div>
                {isTTSAvailable() && (
                  <button
                    onClick={() => speak(item.nl)}
                    className="btn-ghost h-9 w-9 shrink-0 !px-0"
                    aria-label={`Lees "${item.nl}" voor`}
                    title="Beluister"
                  >
                    🔊
                  </button>
                )}
              </li>
            ))}
          </ul>
        </div>

        <div className="flex gap-2 border-t border-slate-100 p-4">
          <button onClick={() => toggle(lesson.id)} className="btn-primary flex-1">
            {done ? '✓ Afgerond — ongedaan maken' : 'Markeer als afgerond'}
          </button>
        </div>
      </div>
    </div>
  )
}
