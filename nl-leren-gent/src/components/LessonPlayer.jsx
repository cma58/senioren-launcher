import { useEffect, useMemo, useState } from 'react'
import { isTTSAvailable, speak } from '../lib/speech.js'
import { buildQuiz } from '../lib/quiz.js'
import { useProgress } from '../context/ProgressContext.jsx'
import SpeakingExercise from './SpeakingExercise.jsx'

/**
 * Volledige, interactieve lesspeler — werkt zonder API's of internet.
 *
 * Fasen:
 *   1) 'learn' — kaartjes: NL-woord + audio, tik om vertaling/tip te tonen.
 *   2) 'quiz'  — meerkeuzevragen (indien mogelijk voor deze les).
 *   3) 'done'  — samenvatting + de les wordt afgevinkt.
 */
export default function LessonPlayer({ lesson, onClose, onOpenSettings }) {
  const { markDone } = useProgress()
  const isSpeaking = lesson?.type === 'speaking'
  const quiz = useMemo(() => (isSpeaking ? [] : buildQuiz(lesson)), [lesson, isSpeaking])

  const [phase, setPhase] = useState(isSpeaking ? 'speaking' : 'learn')

  // Sluiten met Escape.
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  if (!lesson) return null

  return (
    <div className="fixed inset-0 z-40 flex flex-col bg-slate-50">
      {/* Kop */}
      <div className="flex items-center gap-3 border-b border-slate-200 bg-white px-4 py-3">
        <button onClick={onClose} className="btn-ghost h-9 w-9 !px-0" aria-label="Sluiten">
          ✕
        </button>
        <div className="min-w-0 flex-1">
          <p className="text-xs font-bold uppercase tracking-wide text-slate-400">
            Les {lesson.id}
          </p>
          <h2 className="truncate text-base font-bold text-slate-900">{lesson.title}</h2>
        </div>
      </div>

      <div className="mx-auto flex w-full max-w-lg flex-1 flex-col overflow-hidden px-4">
        {phase === 'speaking' && (
          <SpeakingExercise
            lesson={lesson}
            onFinish={() => setPhase('done')}
            onOpenSettings={onOpenSettings}
          />
        )}
        {phase === 'learn' && (
          <LearnPhase
            lesson={lesson}
            onFinish={() => setPhase(quiz.length ? 'quiz' : 'done')}
            hasQuiz={quiz.length > 0}
          />
        )}
        {phase === 'quiz' && (
          <QuizPhase quiz={quiz} onFinish={() => setPhase('done')} onBack={() => setPhase('learn')} />
        )}
        {phase === 'done' && (
          <DonePhase
            lesson={lesson}
            onClose={() => {
              markDone(lesson.id)
              onClose()
            }}
            onRestart={() => setPhase(isSpeaking ? 'speaking' : 'learn')}
          />
        )}
      </div>
    </div>
  )
}

/* ------------------------------------------------------------------ */
/*  Fase 1 — Leren (kaartjes)                                          */
/* ------------------------------------------------------------------ */
function LearnPhase({ lesson, onFinish, hasQuiz }) {
  const [i, setI] = useState(0)
  const [revealed, setRevealed] = useState(false)
  const item = lesson.items[i]
  const isLast = i === lesson.items.length - 1

  // Automatisch voorlezen bij een nieuw kaartje.
  useEffect(() => {
    setRevealed(false)
    if (item?.nl) speak(item.nl)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [i])

  const next = () => (isLast ? onFinish() : setI((n) => n + 1))
  const prev = () => setI((n) => Math.max(0, n - 1))

  return (
    <div className="flex flex-1 flex-col">
      {/* Voortgangsstippen */}
      <div className="flex items-center justify-center gap-1.5 py-4">
        {lesson.items.map((_, idx) => (
          <span
            key={idx}
            className={`h-1.5 rounded-full transition-all ${
              idx === i ? 'w-5 bg-gent-600' : idx < i ? 'w-1.5 bg-gent-300' : 'w-1.5 bg-slate-200'
            }`}
          />
        ))}
      </div>

      {lesson.intro && i === 0 && (
        <p className="mb-2 text-center text-sm text-slate-500">{lesson.intro}</p>
      )}

      {/* Kaart */}
      <button
        onClick={() => setRevealed((r) => !r)}
        className="card flex flex-1 flex-col items-center justify-center gap-4 p-6 text-center"
      >
        <div className="flex items-center gap-2">
          {item.article && (
            <span className="text-lg font-semibold text-gent-500">{item.article}</span>
          )}
          <span className="text-3xl font-bold text-slate-900">{item.nl}</span>
        </div>
        {typeof item.value === 'number' && (
          <span className="text-5xl font-black text-slate-200">{item.value}</span>
        )}
        {item.pair && <span className="text-lg text-slate-400">↔ {item.pair}</span>}
        {item.ipa && <span className="text-sm text-slate-400">{item.ipa}</span>}

        {revealed ? (
          <div className="mt-1 space-y-1">
            {(item.darija || item.darijaLat) && (
              <p className="text-lg text-slate-700">
                {item.darija && <span className="rtl">{item.darija}</span>}
                {item.darija && item.darijaLat && ' · '}
                {item.darijaLat && <span className="italic">{item.darijaLat}</span>}
              </p>
            )}
            {item.tip && <p className="text-sm text-emerald-700">💡 {item.tip}</p>}
            {item.example && <p className="text-sm text-slate-400">bv. {item.example}</p>}
          </div>
        ) : (
          <span className="mt-1 text-xs text-slate-400">tik om de vertaling te zien</span>
        )}
      </button>

      {/* Audio + navigatie */}
      <div className="flex items-center gap-2 py-4">
        <button onClick={prev} disabled={i === 0} className="btn-ghost h-12 w-12 !px-0" aria-label="Vorige">
          ←
        </button>
        {isTTSAvailable() && (
          <button onClick={() => speak(item.nl)} className="btn-ghost flex-1 h-12" aria-label="Beluister">
            🔊 Beluister
          </button>
        )}
        <button onClick={next} className="btn-primary flex-1 h-12">
          {isLast ? (hasQuiz ? 'Naar de oefening →' : 'Klaar →') : 'Volgende →'}
        </button>
      </div>
    </div>
  )
}

/* ------------------------------------------------------------------ */
/*  Fase 2 — Quiz (meerkeuze)                                          */
/* ------------------------------------------------------------------ */
function QuizPhase({ quiz, onFinish, onBack }) {
  const [i, setI] = useState(0)
  const [picked, setPicked] = useState(null)
  const [score, setScore] = useState(0)
  const q = quiz[i]
  const isLast = i === quiz.length - 1
  const answered = picked !== null

  function choose(option) {
    if (answered) return
    setPicked(option)
    if (option === q.answer) setScore((s) => s + 1)
    speak(q.answer)
  }

  function next() {
    if (isLast) {
      onFinish()
      return
    }
    setI((n) => n + 1)
    setPicked(null)
  }

  return (
    <div className="flex flex-1 flex-col">
      <div className="flex items-center justify-between py-4">
        <button onClick={onBack} className="text-sm font-semibold text-slate-400 hover:text-slate-600">
          ← terug naar leren
        </button>
        <span className="text-sm font-semibold text-slate-500">
          Vraag {i + 1}/{quiz.length}
        </span>
      </div>

      <div className="card flex flex-col items-center gap-2 p-6 text-center">
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
          Wat is dit in het Nederlands?
        </p>
        <p className={`text-3xl font-bold text-slate-900 ${q.rtl ? 'rtl' : ''}`}>{q.prompt}</p>
      </div>

      <div className="mt-4 grid gap-2.5">
        {q.options.map((option) => {
          let style = 'border-slate-200 bg-white hover:border-gent-300'
          if (answered) {
            if (option === q.answer) style = 'border-emerald-500 bg-emerald-50 text-emerald-800'
            else if (option === picked) style = 'border-rose-400 bg-rose-50 text-rose-700'
            else style = 'border-slate-200 bg-white opacity-60'
          }
          return (
            <button
              key={option}
              onClick={() => choose(option)}
              disabled={answered}
              className={`flex items-center justify-between rounded-xl border-2 px-4 py-3.5 text-left font-semibold transition ${style}`}
            >
              <span>{option}</span>
              {answered && option === q.answer && <span>✓</span>}
              {answered && option === picked && option !== q.answer && <span>✕</span>}
            </button>
          )
        })}
      </div>

      <div className="mt-auto py-4">
        <button onClick={next} disabled={!answered} className="btn-primary h-12 w-full">
          {isLast ? 'Bekijk resultaat →' : 'Volgende vraag →'}
        </button>
      </div>

      <ScoreBadge score={score} total={quiz.length} />
    </div>
  )
}

function ScoreBadge({ score, total }) {
  return (
    <p className="pb-4 text-center text-xs text-slate-400">
      Score: {score}/{total}
    </p>
  )
}

/* ------------------------------------------------------------------ */
/*  Fase 3 — Klaar                                                     */
/* ------------------------------------------------------------------ */
function DonePhase({ lesson, onClose, onRestart }) {
  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-4 text-center">
      <div className="grid h-20 w-20 place-items-center rounded-full bg-emerald-100 text-4xl">🎉</div>
      <h3 className="text-2xl font-bold text-slate-900">Goed gedaan!</h3>
      <p className="max-w-xs text-slate-500">
        Je hebt de les <span className="font-semibold">{lesson.title}</span> afgerond.
      </p>
      <div className="mt-2 flex w-full max-w-xs flex-col gap-2">
        <button onClick={onClose} className="btn-primary h-12">
          ✓ Afronden en terug
        </button>
        <button onClick={onRestart} className="btn-ghost h-12">
          Nog eens oefenen
        </button>
      </div>
    </div>
  )
}
