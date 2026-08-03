/**
 * Bovenbalk van de app. Toont de titel en (optioneel) een terugknop
 * wanneer je in een level/module zit.
 */
export default function Header({ onBack, subtitle }) {
  return (
    <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/85 backdrop-blur">
      <div className="mx-auto flex max-w-2xl items-center gap-3 px-4 py-3">
        {onBack ? (
          <button
            onClick={onBack}
            className="btn-ghost -ml-2 h-9 w-9 !px-0"
            aria-label="Terug"
          >
            <span aria-hidden="true">←</span>
          </button>
        ) : (
          <span
            className="grid h-9 w-9 place-items-center rounded-xl bg-gent-600 text-sm font-bold text-saffraan-400"
            aria-hidden="true"
          >
            NL
          </span>
        )}
        <div className="min-w-0">
          <h1 className="truncate text-base font-bold leading-tight text-slate-900">
            Nederlands leren in Gent
          </h1>
          {subtitle && (
            <p className="truncate text-xs text-slate-500">{subtitle}</p>
          )}
        </div>
      </div>
    </header>
  )
}
