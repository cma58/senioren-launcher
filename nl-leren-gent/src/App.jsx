import { useState } from 'react'
import Header from './components/Header.jsx'
import Dashboard from './components/Dashboard.jsx'
import LevelView from './components/LevelView.jsx'
import LessonPreview from './components/LessonPreview.jsx'

/**
 * Wortelcomponent. Eenvoudige navigatie via lokale state (geen router nodig
 * voor deze omvang): 'dashboard' <-> een geopend niveau, met een optioneel
 * les-voorbeeldpaneel erbovenop.
 */
export default function App() {
  const [activeLevel, setActiveLevel] = useState(null)
  const [activeLesson, setActiveLesson] = useState(null)

  return (
    <div className="min-h-dvh">
      <Header
        onBack={activeLevel ? () => setActiveLevel(null) : undefined}
        subtitle={activeLevel ? `${activeLevel.title} · ${activeLevel.subtitle}` : undefined}
      />

      <main className="pb-16">
        {activeLevel ? (
          <LevelView level={activeLevel} onOpenLesson={setActiveLesson} />
        ) : (
          <Dashboard onOpenLevel={setActiveLevel} />
        )}
      </main>

      {activeLesson && (
        <LessonPreview lesson={activeLesson} onClose={() => setActiveLesson(null)} />
      )}
    </div>
  )
}
