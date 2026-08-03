import { createContext, useContext, useEffect, useMemo, useState } from 'react'

/**
 * Houdt bij welke lessen de gebruiker heeft afgerond.
 * Voorlopig lokaal opgeslagen in de browser (localStorage), zodat de
 * voortgang bewaard blijft zonder account. Later eenvoudig te vervangen
 * door een backend zonder de rest van de app te wijzigen.
 */

const STORAGE_KEY = 'nl-gent:progress:v1'
const ProgressContext = createContext(null)

function loadInitial() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch {
    return {}
  }
}

export function ProgressProvider({ children }) {
  // { [lessonId]: true }
  const [completed, setCompleted] = useState(loadInitial)

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(completed))
    } catch {
      /* opslag kan geweigerd zijn (privémodus) — geen probleem */
    }
  }, [completed])

  const api = useMemo(
    () => ({
      completed,
      isDone: (lessonId) => Boolean(completed[lessonId]),
      markDone: (lessonId) =>
        setCompleted((prev) => ({ ...prev, [lessonId]: true })),
      toggle: (lessonId) =>
        setCompleted((prev) => {
          const next = { ...prev }
          if (next[lessonId]) delete next[lessonId]
          else next[lessonId] = true
          return next
        }),
      reset: () => setCompleted({}),
      /** Aandeel afgeronde lessen (0–1) voor een lijst met les-id's. */
      ratioFor: (lessonIds) => {
        if (!lessonIds.length) return 0
        const done = lessonIds.filter((id) => completed[id]).length
        return done / lessonIds.length
      },
      countFor: (lessonIds) => lessonIds.filter((id) => completed[id]).length,
    }),
    [completed],
  )

  return <ProgressContext.Provider value={api}>{children}</ProgressContext.Provider>
}

export function useProgress() {
  const ctx = useContext(ProgressContext)
  if (!ctx) throw new Error('useProgress moet binnen <ProgressProvider> gebruikt worden')
  return ctx
}
