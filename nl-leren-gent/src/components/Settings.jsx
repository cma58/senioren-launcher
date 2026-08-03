import { useEffect, useState } from 'react'
import {
  getGeminiKey,
  getGroqKey,
  setGeminiKey,
  setGroqKey,
} from '../lib/config.js'

/**
 * Instellingenpaneel om de (gratis) API-sleutels in te vullen.
 * De sleutels worden lokaal in de browser bewaard — niet verstuurd naar ons.
 */
export default function Settings({ onClose }) {
  const [gemini, setGemini] = useState('')
  const [groq, setGroq] = useState('')
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    setGemini(getGeminiKey())
    setGroq(getGroqKey())
  }, [])

  function save() {
    setGeminiKey(gemini)
    setGroqKey(groq)
    setSaved(true)
    setTimeout(() => setSaved(false), 1500)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center">
      <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-t-3xl bg-white shadow-xl sm:rounded-3xl">
        <div className="flex items-center justify-between border-b border-slate-100 p-5">
          <h3 className="text-lg font-bold text-slate-900">Instellingen · API-sleutels</h3>
          <button onClick={onClose} className="btn-ghost h-9 w-9 !px-0" aria-label="Sluiten">
            ✕
          </button>
        </div>

        <div className="space-y-5 overflow-y-auto p-5">
          <p className="text-sm text-slate-500">
            Deze sleutels zijn nodig voor de spreekoefening (microfoon + AI-feedback). Ze zijn
            gratis en worden alleen op dit toestel bewaard.
          </p>

          <div>
            <label className="mb-1 block text-sm font-semibold text-slate-700">
              Google Gemini-sleutel <span className="text-slate-400">(AI-feedback)</span>
            </label>
            <input
              value={gemini}
              onChange={(e) => setGemini(e.target.value)}
              type="password"
              autoComplete="off"
              spellCheck={false}
              placeholder="AIza…"
              className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-mono text-sm focus:border-gent-400 focus:outline-none"
            />
            <a
              href="https://aistudio.google.com/app/apikey"
              target="_blank"
              rel="noopener noreferrer"
              className="mt-1 inline-block text-xs font-semibold text-gent-600 underline"
            >
              → Gratis sleutel maken op Google AI Studio
            </a>
          </div>

          <div>
            <label className="mb-1 block text-sm font-semibold text-slate-700">
              Groq-sleutel <span className="text-slate-400">(spraakherkenning)</span>
            </label>
            <input
              value={groq}
              onChange={(e) => setGroq(e.target.value)}
              type="password"
              autoComplete="off"
              spellCheck={false}
              placeholder="gsk_…"
              className="w-full rounded-xl border-2 border-slate-200 px-4 py-3 font-mono text-sm focus:border-gent-400 focus:outline-none"
            />
            <a
              href="https://console.groq.com/keys"
              target="_blank"
              rel="noopener noreferrer"
              className="mt-1 inline-block text-xs font-semibold text-gent-600 underline"
            >
              → Gratis sleutel maken op Groq Console
            </a>
          </div>

          <p className="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
            Tip: je kunt de app al zonder sleutels gebruiken voor alle lessen en quizzen. De
            sleutels zijn enkel voor de spreekoefening met AI.
          </p>
        </div>

        <div className="border-t border-slate-100 p-4">
          <button onClick={save} className="btn-primary h-12 w-full">
            {saved ? '✓ Opgeslagen' : 'Opslaan'}
          </button>
        </div>
      </div>
    </div>
  )
}
