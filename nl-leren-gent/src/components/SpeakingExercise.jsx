import { useEffect, useState } from 'react'
import { isTTSAvailable, speak } from '../lib/speech.js'
import { transcribeAudio } from '../lib/groq.js'
import { evaluateAnswer } from '../lib/gemini.js'
import { hasGemini, hasGroq } from '../lib/config.js'
import { useRecorder } from '../hooks/useRecorder.js'

/**
 * Spreekoefening voor lessen van het type 'speaking'.
 *
 * Flow per opdracht:
 *   1) de leerling hoort/leest de doelzin;
 *   2) ze spreekt in (microfoon) → Groq Whisper zet het om naar tekst,
 *      of ze typt het antwoord (terugval zonder microfoonsleutel);
 *   3) Gemini beoordeelt het antwoord als een geduldige NT2-docent.
 */
export default function SpeakingExercise({ lesson, onFinish, onOpenSettings }) {
  const [i, setI] = useState(0)
  const item = lesson.items[i]
  const isLast = i === lesson.items.length - 1

  // Doelzin = het concrete verwachte antwoord, of de zin zelf (naspreken).
  const target = item.answer || item.nl
  const speakable = item.answer && item.answer.includes('...') ? item.nl : target

  const recorder = useRecorder()
  const [status, setStatus] = useState('idle') // idle | busy | result | error
  const [transcript, setTranscript] = useState('')
  const [typed, setTyped] = useState('')
  const [feedback, setFeedback] = useState(null)
  const [errorMsg, setErrorMsg] = useState('')

  // Alles resetten bij een nieuwe opdracht.
  useEffect(() => {
    setStatus('idle')
    setTranscript('')
    setTyped('')
    setFeedback(null)
    setErrorMsg('')
  }, [i])

  const canRecord = hasGroq() && recorder.supported
  const canEvaluate = hasGemini()

  async function check(answerText) {
    if (!answerText?.trim()) return
    if (!canEvaluate) {
      setErrorMsg('Er is nog geen Gemini-sleutel ingesteld. Open de instellingen (⚙️).')
      setStatus('error')
      return
    }
    setStatus('busy')
    setErrorMsg('')
    try {
      const result = await evaluateAnswer(answerText, target, lesson.intro || item.nl)
      setFeedback(result)
      setStatus('result')
      if (isTTSAvailable() && result.correction) speak(result.correction)
    } catch (e) {
      setErrorMsg(friendlyError(e))
      setStatus('error')
    }
  }

  async function handleRecordToggle() {
    if (recorder.recording) {
      // stoppen → transcriberen → beoordelen
      setStatus('busy')
      const blob = await recorder.stop()
      if (!blob) {
        setStatus('idle')
        return
      }
      try {
        const text = await transcribeAudio(blob)
        setTranscript(text)
        if (text) await check(text)
        else {
          setErrorMsg('Ik heb niets verstaan. Probeer opnieuw, wat luider.')
          setStatus('error')
        }
      } catch (e) {
        setErrorMsg(friendlyError(e))
        setStatus('error')
      }
    } else {
      setFeedback(null)
      setTranscript('')
      recorder.start()
    }
  }

  function next() {
    if (isLast) onFinish()
    else setI((n) => n + 1)
  }

  const noKeys = !canEvaluate && !canRecord

  return (
    <div className="flex flex-1 flex-col">
      {/* Voortgang */}
      <div className="flex items-center justify-center gap-1.5 py-4">
        {lesson.items.map((_, idx) => (
          <span
            key={idx}
            className={`h-1.5 rounded-full transition-all ${
              idx === i ? 'w-5 bg-saffraan-500' : idx < i ? 'w-1.5 bg-saffraan-300' : 'w-1.5 bg-slate-200'
            }`}
          />
        ))}
      </div>

      {/* Opdracht */}
      <div className="card p-5 text-center">
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
          🎙️ Spreek deze zin
        </p>
        <p className="mt-2 text-2xl font-bold text-slate-900">{speakable}</p>
        {item.darija && <p className="rtl mt-1 text-slate-500">{item.darija}</p>}
        {isTTSAvailable() && (
          <button onClick={() => speak(speakable)} className="btn-ghost mt-3">
            🔊 Beluister voorbeeld
          </button>
        )}
      </div>

      {/* Instellingen-hint als er geen sleutels zijn */}
      {noKeys && (
        <div className="mt-4 rounded-xl bg-amber-50 p-4 text-sm text-amber-800">
          Voor de spreekoefening met AI heb je gratis sleutels nodig.{' '}
          <button onClick={onOpenSettings} className="font-semibold underline">
            Open de instellingen (⚙️)
          </button>{' '}
          om ze in te vullen. Je kunt hieronder ook typen om te oefenen.
        </div>
      )}

      {/* Opnemen of typen */}
      <div className="mt-4">
        {canRecord ? (
          <button
            onClick={handleRecordToggle}
            disabled={status === 'busy' && !recorder.recording}
            className={`flex h-16 w-full items-center justify-center gap-3 rounded-2xl text-lg font-bold text-white transition ${
              recorder.recording ? 'animate-pulse bg-rose-600' : 'bg-saffraan-500 hover:bg-saffraan-600'
            } disabled:opacity-50`}
          >
            {recorder.recording ? '⏹️ Stop & controleer' : '🎙️ Neem op'}
          </button>
        ) : (
          <div className="flex gap-2">
            <input
              value={typed}
              onChange={(e) => setTyped(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && check(typed)}
              placeholder="Typ hier je antwoord…"
              className="flex-1 rounded-xl border-2 border-slate-200 px-4 py-3 focus:border-saffraan-400 focus:outline-none"
            />
            <button onClick={() => check(typed)} className="btn-primary px-5">
              Check
            </button>
          </div>
        )}
        {recorder.error && <p className="mt-2 text-sm text-rose-600">{recorder.error}</p>}
      </div>

      {/* Status / resultaat */}
      <div className="mt-4 flex-1">
        {status === 'busy' && (
          <p className="text-center text-sm text-slate-500">⏳ Even geduld… ik luister en controleer.</p>
        )}

        {transcript && (
          <p className="mb-3 rounded-lg bg-slate-100 p-3 text-sm text-slate-600">
            Ik hoorde: <span className="font-semibold">“{transcript}”</span>
          </p>
        )}

        {status === 'error' && errorMsg && (
          <p className="rounded-lg bg-rose-50 p-3 text-sm text-rose-700">{errorMsg}</p>
        )}

        {status === 'result' && feedback && (
          <div
            className={`rounded-xl p-4 ${
              feedback.correct ? 'bg-emerald-50' : 'bg-amber-50'
            }`}
          >
            <p className={`font-bold ${feedback.correct ? 'text-emerald-800' : 'text-amber-900'}`}>
              {feedback.correct ? '✅ Goed gedaan!' : '✍️ Bijna — kijk even:'}
            </p>
            {feedback.feedback_nl && (
              <p className="mt-1 text-sm text-slate-700">{feedback.feedback_nl}</p>
            )}
            {feedback.correction && (
              <p className="mt-1 text-sm text-slate-800">
                Correct: <span className="font-semibold">{feedback.correction}</span>
              </p>
            )}
            {feedback.feedback_darija && (
              <p className="rtl mt-2 text-sm text-slate-600">{feedback.feedback_darija}</p>
            )}
          </div>
        )}
      </div>

      {/* Navigatie */}
      <div className="flex gap-2 py-4">
        <button onClick={next} className="btn-ghost flex-1 h-12">
          Overslaan
        </button>
        <button
          onClick={next}
          disabled={status !== 'result'}
          className="btn-primary flex-1 h-12"
        >
          {isLast ? 'Klaar →' : 'Volgende →'}
        </button>
      </div>
    </div>
  )
}

function friendlyError(e) {
  const msg = String(e?.message || e)
  if (msg.includes('GEEN_GROQ_SLEUTEL')) return 'Geen Groq-sleutel ingesteld (⚙️).'
  if (msg.includes('GEEN_GEMINI_SLEUTEL')) return 'Geen Gemini-sleutel ingesteld (⚙️).'
  if (msg.includes('401') || msg.includes('403'))
    return 'De sleutel wordt geweigerd. Controleer of je hem juist hebt gekopieerd (⚙️).'
  if (msg.includes('429')) return 'Even te veel verzoeken. Wacht een momentje en probeer opnieuw.'
  return 'Er ging iets mis. Controleer je internet en je sleutels (⚙️).'
}
