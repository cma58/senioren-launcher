/**
 * ============================================================================
 *  LEERLIJN / CURRICULUM  —  Nederlands leren in Gent (voor Darija-sprekers)
 * ============================================================================
 *
 *  Doelgroep : Marokkaans-Darija sprekende beginners (uit Oujda) die naar
 *              Gent (Vlaanderen) verhuizen.
 *  Focus     : Vlaams-Nederlands (nl-BE), niveau 0 -> A1.1.
 *
 *  SCHEMA
 *  ------
 *  curriculum = {
 *    meta:   { ... },              // algemene info
 *    levels: [ Level, ... ]
 *  }
 *
 *  Level = {
 *    id:          string,          // uniek, bv. "niveau-0"
 *    order:       number,          // volgorde in de UI
 *    title:       string,
 *    subtitle:    string,
 *    description: string,
 *    cefr:        string|null,     // Europees referentieniveau (bv. "A1.1")
 *    icon:        string,          // emoji voor de UI
 *    accent:      string,          // tailwind-kleurnaam voor accenten
 *    modules:     [ Module, ... ]
 *  }
 *
 *  Module = {
 *    id:          string,          // bv. "0.1"
 *    title:       string,
 *    goal:        string,          // leerdoel in één zin
 *    icon:        string,
 *    lessons:     [ Lesson, ... ]
 *  }
 *
 *  Lesson = {
 *    id:          string,          // bv. "0.1.1"
 *    title:       string,
 *    type:        LessonType,      // bepaalt welke oefen-component wordt getoond
 *    intro:       string,          // korte uitleg (NL)
 *    darijaNote?: string,          // extra uitleg in het Darija/Arabisch (optioneel)
 *    items:       [ Item, ... ]    // vocabulaire / voorbeeldzinnen / oefeningen
 *  }
 *
 *  LessonType (voor Stap 2 — de interactieve components):
 *    'phonetics'  -> klank-/uitspraakoefening (minimale paren)
 *    'vocab'      -> woordenschat met audio
 *    'phrases'    -> voorbeeldzinnen naspreken
 *    'numbers'    -> getallen
 *    'grammar'    -> grammaticaregel + voorbeelden
 *    'speaking'   -> spreekoefening (Whisper + Gemini)
 *    'quiz'       -> meerkeuze / invuloefening
 *
 *  Item (flexibel per lestype — alle velden optioneel behalve 'nl'):
 *    nl:         string            // het Nederlandse woord/zin (verplicht)
 *    darija?:    string            // vertaling in het Darija (Arabisch schrift)
 *    darijaLat?: string            // Darija in Latijns schrift (transliteratie)
 *    ipa?:       string            // fonetische uitspraak
 *    tip?:       string            // uitspraaktip afgestemd op Darija-sprekers
 *    article?:   'de' | 'het'      // lidwoord (voor zelfstandige naamwoorden)
 *    example?:   string            // voorbeeldzin
 *    pair?:      string            // minimaal paar (contrastwoord bij fonetiek)
 *    options?:   string[]          // antwoordopties (bij quiz)
 *    answer?:    string            // correct antwoord (bij quiz/speaking)
 * ============================================================================
 */

export const LESSON_TYPES = {
  PHONETICS: 'phonetics',
  VOCAB: 'vocab',
  PHRASES: 'phrases',
  NUMBERS: 'numbers',
  GRAMMAR: 'grammar',
  SPEAKING: 'speaking',
  QUIZ: 'quiz',
}

const curriculum = {
  meta: {
    appName: 'Nederlands leren in Gent',
    targetLang: 'nl-BE',
    learnerL1: 'Marokkaans-Arabisch (Darija)',
    city: 'Gent',
    version: '0.1.0',
  },

  levels: [
    // =======================================================================
    //  NIVEAU 0 — ABSOLUTE BASIS & FONETIEK
    // =======================================================================
    {
      id: 'niveau-0',
      order: 0,
      title: 'Niveau 0',
      subtitle: 'Absolute basis & fonetiek',
      description:
        'De klanken, de begroetingen, de getallen en je eerste woorden. De perfecte start als je nog geen Nederlands kent.',
      cefr: null,
      icon: '🔤',
      accent: 'gent',
      modules: [
        // ---- Module 0.1 -------------------------------------------------
        {
          id: '0.1',
          title: 'Klankleer & uitspraak',
          goal: 'De Nederlandse klanken herkennen en uitspreken — met tips voor Darija-sprekers.',
          icon: '🗣️',
          lessons: [
            {
              id: '0.1.1',
              title: 'Korte vs. lange klinkers',
              type: LESSON_TYPES.PHONETICS,
              intro:
                'In het Nederlands verandert de betekenis van een woord als een klinker kort of lang is. Luister goed naar het verschil.',
              darijaNote: 'الفرق بين الصوت القصير والطويل يغيّر معنى الكلمة في الهولندية.',
              items: [
                { nl: 'man', pair: 'maan', ipa: '/mɑn/ – /maːn/', tip: 'Kort "a" zoals in Darija «مانا»; lange "aa" houd je langer aan.', darijaLat: 'rajel – qamar' },
                { nl: 'bos', pair: 'boos', ipa: '/bɔs/ – /boːs/', tip: 'Korte "o" is open; lange "oo" is ronder en langer.', darijaLat: 'ghaba – ghadeb' },
                { nl: 'pit', pair: 'piet', ipa: '/pɪt/ – /pit/', tip: 'Korte "i" ligt tussen i en e; lange "ie" is een heldere «i».' },
                { nl: 'bus', pair: 'buur', ipa: '/bʏs/ – /byːr/', tip: 'De "u" bestaat niet in Darija: rond je lippen als bij «oe» maar zeg «i».' },
                { nl: 'les', pair: 'lees', ipa: '/lɛs/ – /leːs/', tip: 'Korte "e" zoals in «best»; lange "ee" langer aanhouden.' },
              ],
            },
            {
              id: '0.1.2',
              title: 'Tweeklanken (UI, EU, IE, IJ/EI, OE, OU/AU)',
              type: LESSON_TYPES.PHONETICS,
              intro:
                'Tweeklanken zijn twee klinkers die samensmelten tot één klank. Let goed op de stand van je mond.',
              darijaNote:
                'الأصوات المركّبة (diftongen) غير موجودة بهذا الشكل في الدارجة — ركّزي على حركة الفم.',
              items: [
                { nl: 'huis', ipa: '/hœys/', tip: '"ui": begin met «a» en glijd naar «u». Rond je lippen op het einde.', darija: 'دار', darijaLat: 'dar (huis)' },
                { nl: 'deur', ipa: '/døːr/', tip: '"eu": zeg «ee» maar met ronde lippen, zoals bij «oe».', darija: 'باب', darijaLat: 'bab (deur)' },
                { nl: 'niet', ipa: '/nit/', tip: '"ie": een lange, heldere «i» zoals in het Arabische «ي».', darija: 'ماشي', darijaLat: 'machi' },
                { nl: 'trein', pair: 'tijd', ipa: '/trɛin/ – /tɛit/', tip: '"ei" en "ij" klinken hetzelfde: begin bij «e» en glijd naar «i».' },
                { nl: 'boek', ipa: '/buk/', tip: '"oe" is als de Arabische «و» in «نور».', darija: 'كتاب', darijaLat: 'ktab (boek)' },
                { nl: 'koud', pair: 'blauw', ipa: '/kɑut/ – /blɑu/', tip: '"ou" en "au" klinken hetzelfde: «a» die naar «u» glijdt.' },
              ],
            },
            {
              id: '0.1.3',
              title: 'Moeilijke medeklinkers (P/B, F/V, G/CH)',
              type: LESSON_TYPES.PHONETICS,
              intro:
                'Sommige medeklinkers zijn lastig omdat ze in het Darija niet (zo) bestaan. Oefen ze met minimale paren.',
              darijaNote:
                'الحرف «P» ما كاينش فالدارجة — كتنطق «B». ركّزي على الفرق باش الكلمة تكون مفهومة.',
              items: [
                { nl: 'pen', pair: 'ben', ipa: '/pɛn/ – /bɛn/', tip: 'P is stemloos (geen trilling in de keel), B is stemhebbend. Leg je hand op je keel om te voelen.' },
                { nl: 'pak', pair: 'bak', ipa: '/pɑk/ – /bɑk/', tip: 'Blaas een klein beetje lucht bij de "p", niet bij de "b".' },
                { nl: 'fee', pair: 'vee', ipa: '/feː/ – /veː/', tip: 'F is stemloos, V is licht stemhebbend. In Vlaanderen liggen ze dicht bij elkaar.' },
                { nl: 'gaan', ipa: '/ɣaːn/', tip: 'De harde "g/ch" is als de Arabische «خ» (khaa), diep in de keel.', darija: 'خ', darijaLat: 'zoals in «خبز» (khubz)' },
                { nl: 'lachen', ipa: '/ˈlɑxə(n)/', tip: '"ch" = dezelfde keelklank als «خ». In Gent iets zachter dan in Nederland.' },
              ],
            },
          ],
        },

        // ---- Module 0.2 -------------------------------------------------
        {
          id: '0.2',
          title: 'Begroetingen & beleefdheid',
          goal: 'Iemand groeten en beleefd reageren in het dagelijks leven.',
          icon: '👋',
          lessons: [
            {
              id: '0.2.1',
              title: 'Groeten',
              type: LESSON_TYPES.VOCAB,
              intro: 'Zo begroet je mensen in Gent, van formeel tot informeel.',
              items: [
                { nl: 'Hallo', darija: 'السلام', darijaLat: 'salam', tip: 'Neutraal, altijd goed.' },
                { nl: 'Dag', darija: 'أهلا', darijaLat: 'ahlan', tip: 'Kan zowel "hallo" als "tot ziens" betekenen.' },
                { nl: 'Goedemorgen', darija: 'صباح الخير', darijaLat: 'sbah lkhir', tip: 'Tot ongeveer 12u.' },
                { nl: 'Goedemiddag', darija: 'مساء الخير', darijaLat: 'msa lkhir', tip: 'Van de middag tot de avond.' },
                { nl: 'Goedenavond', darija: 'مسا الخير', darijaLat: 'msa lkhir', tip: "'s Avonds." },
                { nl: 'Tot ziens', darija: 'بسلامة', darijaLat: 'bslama', tip: 'Bij het afscheid.' },
                { nl: 'Tot morgen', darija: 'نتلاقاو غدا', darijaLat: 'ntlaqaw ghedda' },
              ],
            },
            {
              id: '0.2.2',
              title: 'Beleefdheid',
              type: LESSON_TYPES.VOCAB,
              intro: 'Beleefde woorden die je elke dag nodig hebt.',
              items: [
                { nl: 'Alstublieft', darija: 'من فضلك', darijaLat: 'men fadlek', tip: 'Formeel (met "u").' },
                { nl: 'Alsjeblieft', darija: 'عافاك', darijaLat: 'afak', tip: 'Informeel (met "je"). Ook: "hier je" als je iets geeft.' },
                { nl: 'Dank u wel', darija: 'شكرا', darijaLat: 'choukran', tip: 'Formeel bedanken.' },
                { nl: 'Dank je wel', darija: 'شكرا بزاف', darijaLat: 'choukran bezzaf', tip: 'Informeel bedanken.' },
                { nl: 'Graag gedaan', darija: 'بلا جميل', darijaLat: 'bla jmil', tip: 'Antwoord op "dank u".' },
                { nl: 'Sorry', darija: 'سمح ليا', darijaLat: 'smeh liya' },
                { nl: 'Pardon', darija: 'سمح ليا', darijaLat: 'smeh liya', tip: 'Om langs iemand te gaan of iets te vragen.' },
              ],
            },
          ],
        },

        // ---- Module 0.3 -------------------------------------------------
        {
          id: '0.3',
          title: 'Getallen 0–20',
          goal: 'Tot twintig tellen en getallen herkennen.',
          icon: '🔢',
          lessons: [
            {
              id: '0.3.1',
              title: 'Tellen van 0 tot 20',
              type: LESSON_TYPES.NUMBERS,
              intro: 'Luister en spreek elk getal na. Let op: "twaalf" en "dertien" klinken anders dan je denkt.',
              items: [
                { nl: 'nul', value: 0, darija: 'صفر', darijaLat: 'sifr' },
                { nl: 'een', value: 1, darija: 'واحد', darijaLat: 'wahed' },
                { nl: 'twee', value: 2, darija: 'جوج', darijaLat: 'jouj' },
                { nl: 'drie', value: 3, darija: 'تلاتة', darijaLat: 'tlata' },
                { nl: 'vier', value: 4, darija: 'ربعة', darijaLat: 'reb3a' },
                { nl: 'vijf', value: 5, darija: 'خمسة', darijaLat: 'khamsa' },
                { nl: 'zes', value: 6, darija: 'ستة', darijaLat: 'setta' },
                { nl: 'zeven', value: 7, darija: 'سبعة', darijaLat: 'seb3a' },
                { nl: 'acht', value: 8, darija: 'تمنية', darijaLat: 'tmenya' },
                { nl: 'negen', value: 9, darija: 'تسعة', darijaLat: 'tes3a' },
                { nl: 'tien', value: 10, darija: 'عشرة', darijaLat: '3echra' },
                { nl: 'elf', value: 11, darija: 'حداش', darijaLat: 'hdach' },
                { nl: 'twaalf', value: 12, darija: 'طناش', darijaLat: 'tnach' },
                { nl: 'dertien', value: 13, darija: 'تلطاش', darijaLat: 'teltach' },
                { nl: 'veertien', value: 14, darija: 'ربعطاش', darijaLat: 'rbe3tach' },
                { nl: 'vijftien', value: 15, darija: 'خمسطاش', darijaLat: 'khmestach' },
                { nl: 'zestien', value: 16, darija: 'سطاش', darijaLat: 'settach' },
                { nl: 'zeventien', value: 17, darija: 'سبعطاش', darijaLat: 'sbe3tach' },
                { nl: 'achttien', value: 18, darija: 'تمنطاش', darijaLat: 'tmentach' },
                { nl: 'negentien', value: 19, darija: 'تسعطاش', darijaLat: 'tse3tach' },
                { nl: 'twintig', value: 20, darija: 'عشرين', darijaLat: '3echrin' },
              ],
            },
          ],
        },

        // ---- Module 0.4 -------------------------------------------------
        {
          id: '0.4',
          title: 'Vraagwoorden & omgeving',
          goal: 'De basisvraagwoorden en tien woorden in en om het huis.',
          icon: '🏠',
          lessons: [
            {
              id: '0.4.1',
              title: 'Vraagwoorden',
              type: LESSON_TYPES.VOCAB,
              intro: 'Met deze woorden stel je je eerste vragen.',
              items: [
                { nl: 'Wie?', darija: 'شكون؟', darijaLat: 'chkoun?', example: 'Wie ben jij?' },
                { nl: 'Wat?', darija: 'شنو؟', darijaLat: 'chnou?', example: 'Wat is dat?' },
                { nl: 'Waar?', darija: 'فين؟', darijaLat: 'fin?', example: 'Waar woon je?' },
                { nl: 'Wanneer?', darija: 'إمتى؟', darijaLat: 'imta?', example: 'Wanneer kom je?' },
                { nl: 'Hoe?', darija: 'كيفاش؟', darijaLat: 'kifach?', example: 'Hoe gaat het?' },
              ],
            },
            {
              id: '0.4.2',
              title: 'In huis (10 basiswoorden)',
              type: LESSON_TYPES.VOCAB,
              intro: 'Tien dingen die je thuis ziet. Let op het lidwoord (de/het).',
              items: [
                { nl: 'tafel', article: 'de', darija: 'الطبلة', darijaLat: 'tebla' },
                { nl: 'stoel', article: 'de', darija: 'الكرسي', darijaLat: 'kursi' },
                { nl: 'raam', article: 'het', darija: 'الشرجم', darijaLat: 'chergem' },
                { nl: 'deur', article: 'de', darija: 'الباب', darijaLat: 'bab' },
                { nl: 'bed', article: 'het', darija: 'الناموسية', darijaLat: 'namousiya' },
                { nl: 'lamp', article: 'de', darija: 'الضو', darijaLat: 'dou' },
                { nl: 'keuken', article: 'de', darija: 'الكوزينة', darijaLat: 'kuzina' },
                { nl: 'bad', article: 'het', darija: 'الحمام', darijaLat: 'hammam' },
                { nl: 'muur', article: 'de', darija: 'الحيط', darijaLat: 'hit' },
                { nl: 'vloer', article: 'de', darija: 'الأرض', darijaLat: 'lard' },
              ],
            },
          ],
        },
      ],
    },

    // =======================================================================
    //  NIVEAU 1 — EERSTE COMMUNICATIE (A1.1, Gent/Vlaanderen)
    // =======================================================================
    {
      id: 'niveau-1',
      order: 1,
      title: 'Niveau 1',
      subtitle: 'Eerste communicatie',
      description:
        'Jezelf voorstellen, over je familie praten, eenvoudige zinnen maken en boodschappen doen in Gent.',
      cefr: 'A1.1',
      icon: '💬',
      accent: 'saffraan',
      modules: [
        // ---- Module 1.1 -------------------------------------------------
        {
          id: '1.1',
          title: 'Jezelf voorstellen',
          goal: 'Vertellen wie je bent, waar je vandaan komt en waar je woont.',
          icon: '🙋‍♀️',
          lessons: [
            {
              id: '1.1.1',
              title: 'Wie ben ik?',
              type: LESSON_TYPES.PHRASES,
              intro: 'De zinnen die je nodig hebt om jezelf voor te stellen in Gent.',
              items: [
                { nl: 'Ik ben Fatima.', darija: 'أنا فاطمة', darijaLat: 'ana Fatima' },
                { nl: 'Mijn naam is Fatima.', darija: 'سميتي فاطمة', darijaLat: 'smiti Fatima' },
                { nl: 'Ik kom uit Marokko.', darija: 'أنا من المغرب', darijaLat: 'ana men lmaghrib' },
                { nl: 'Ik kom uit Oujda.', darija: 'أنا من وجدة', darijaLat: 'ana men Oujda' },
                { nl: 'Ik woon in Gent.', darija: 'أنا ساكنة فگاند', darijaLat: 'ana sakna f Gent' },
                { nl: 'Ik spreek Darija en een beetje Nederlands.', darija: 'كنهضر بالدارجة وشوية بالهولندية', darijaLat: 'kanhder b darija w chwiya b hollandiya' },
              ],
            },
            {
              id: '1.1.2',
              title: 'Spreekoefening: stel jezelf voor',
              type: LESSON_TYPES.SPEAKING,
              intro: 'Spreek je eigen voorstelling in. De app luistert en helpt je verbeteren.',
              items: [
                { nl: 'Stel jezelf voor in drie zinnen.', answer: 'Ik ben ... Ik kom uit Oujda. Ik woon in Gent.' },
              ],
            },
          ],
        },

        // ---- Module 1.2 -------------------------------------------------
        {
          id: '1.2',
          title: 'Familie & gezin',
          goal: 'Praten over je familie en bezit aangeven (mijn, jouw, uw).',
          icon: '👨‍👩‍👧‍👦',
          lessons: [
            {
              id: '1.2.1',
              title: 'De familieleden',
              type: LESSON_TYPES.VOCAB,
              intro: 'Woordenschat over het gezin.',
              items: [
                { nl: 'man', article: 'de', darija: 'الراجل', darijaLat: 'rajel' },
                { nl: 'vrouw', article: 'de', darija: 'المرا', darijaLat: 'mra' },
                { nl: 'kind', article: 'het', darija: 'الولد/الطفل', darijaLat: 'weld / tfel' },
                { nl: 'zoon', article: 'de', darija: 'الولد', darijaLat: 'weld' },
                { nl: 'dochter', article: 'de', darija: 'البنت', darijaLat: 'bent' },
                { nl: 'broer', article: 'de', darija: 'الخو', darijaLat: 'khou' },
                { nl: 'zus', article: 'de', darija: 'الأخت', darijaLat: 'oukht' },
                { nl: 'moeder', article: 'de', darija: 'الأم', darijaLat: 'oumm / mama' },
                { nl: 'vader', article: 'de', darija: 'الأب', darijaLat: 'bba / baba' },
              ],
            },
            {
              id: '1.2.2',
              title: 'Bezittelijke voornaamwoorden',
              type: LESSON_TYPES.GRAMMAR,
              intro:
                'Met "mijn", "jouw" en "uw" laat je zien van wie iets is. "Uw" is beleefd (formeel).',
              darijaNote: 'ديالي = mijn، ديالك = jouw، ديالكم (احترام) = uw.',
              items: [
                { nl: 'mijn', darija: 'ديالي', darijaLat: 'dyali', example: 'mijn broer / mijn kind' },
                { nl: 'jouw', darija: 'ديالك', darijaLat: 'dyalek', example: 'jouw zus / jouw huis' },
                { nl: 'uw', darija: 'ديالكم', darijaLat: 'dyalkoum', example: 'uw naam (beleefd)' },
                { nl: 'Dat is mijn dochter.', example: 'Voorbeeldzin', darija: 'هادي بنتي', darijaLat: 'hadi benti' },
                { nl: 'Is dat jouw broer?', example: 'Voorbeeldzin', darija: 'واش هادا خوك؟', darijaLat: 'wach hada khouk?' },
              ],
            },
          ],
        },

        // ---- Module 1.3 -------------------------------------------------
        {
          id: '1.3',
          title: 'Basisgrammatica',
          goal: 'De/het, de tegenwoordige tijd en de werkwoorden "zijn" en "hebben".',
          icon: '📘',
          lessons: [
            {
              id: '1.3.1',
              title: 'DE of HET?',
              type: LESSON_TYPES.GRAMMAR,
              intro:
                'Elk zelfstandig naamwoord heeft "de" of "het". Handige regels: mensen zijn (bijna) altijd "de", een meervoud is ALTIJD "de", en een verkleinwoord (-je) is ALTIJD "het".',
              darijaNote:
                'ما كاينش قاعدة كاملة، خاصك تحفظي. ولكن: الجمع دائماً «de»، والتصغير (-je) دائماً «het».',
              items: [
                { nl: 'de man', article: 'de', tip: 'Persoon → de.' },
                { nl: 'de vrouw', article: 'de', tip: 'Persoon → de.' },
                { nl: 'het kind', article: 'het', tip: 'Uitzondering: onzijdig.' },
                { nl: 'de kinderen', article: 'de', tip: 'Meervoud → altijd de.' },
                { nl: 'het tafeltje', article: 'het', tip: 'Verkleinwoord (-je) → altijd het.' },
                { nl: 'het huis', article: 'het' },
                { nl: 'de straat', article: 'de' },
              ],
            },
            {
              id: '1.3.2',
              title: 'Tegenwoordige tijd (stam + t)',
              type: LESSON_TYPES.GRAMMAR,
              intro:
                'Regel: ik = stam. jij/hij/zij = stam + t. wij/jullie/zij (mv) = hele werkwoord. Voorbeeld met "wonen" (stam = woon).',
              items: [
                { nl: 'ik woon', darijaLat: 'wonen → ik woon' },
                { nl: 'jij woont', tip: 'stam + t' },
                { nl: 'hij/zij woont', tip: 'stam + t' },
                { nl: 'wij wonen', tip: 'hele werkwoord' },
                { nl: 'ik werk / jij werkt', darijaLat: 'werken' },
                { nl: 'ik heet / jij heet', tip: 'stam eindigt al op t → geen dubbele t', darijaLat: 'heten' },
                { nl: 'ik spreek / jij spreekt', darijaLat: 'spreken' },
              ],
            },
            {
              id: '1.3.3',
              title: 'Onregelmatig: zijn & hebben',
              type: LESSON_TYPES.GRAMMAR,
              intro:
                'Deze twee werkwoorden zijn onregelmatig en heel belangrijk. Leer ze uit het hoofd.',
              darijaNote: '«zijn» = كون (ana... )، «hebben» = عند.',
              items: [
                { nl: 'ik ben', darija: 'أنا كاين/ة', darijaLat: 'zijn' },
                { nl: 'jij bent', darijaLat: 'zijn' },
                { nl: 'hij/zij is', darijaLat: 'zijn' },
                { nl: 'wij zijn', darijaLat: 'zijn' },
                { nl: 'ik heb', darija: 'عندي', darijaLat: 'hebben' },
                { nl: 'jij hebt', darija: 'عندك', darijaLat: 'hebben' },
                { nl: 'hij/zij heeft', darija: 'عندو/عندها', darijaLat: 'hebben' },
                { nl: 'wij hebben', darija: 'عندنا', darijaLat: 'hebben' },
              ],
            },
          ],
        },

        // ---- Module 1.4 -------------------------------------------------
        {
          id: '1.4',
          title: 'Vragen stellen & boodschappen',
          goal: 'Vragen maken met inversie, tellen tot 100 en boodschappen doen.',
          icon: '🛒',
          lessons: [
            {
              id: '1.4.1',
              title: 'Vragen met inversie',
              type: LESSON_TYPES.GRAMMAR,
              intro:
                'Voor een ja/nee-vraag wissel je het werkwoord en het onderwerp om. "Jij woont" → "Woon jij?"',
              darijaNote: 'باش تسولي سؤال، كتبدلي الفعل والفاعل: «jij woont» → «woon jij?».',
              items: [
                { nl: 'Woon jij in Gent?', example: 'van: jij woont in Gent' },
                { nl: 'Spreek jij Nederlands?', example: 'van: jij spreekt Nederlands' },
                { nl: 'Heb jij kinderen?', example: 'van: jij hebt kinderen' },
                { nl: 'Ben jij van Oujda?', example: 'van: jij bent van Oujda' },
                { nl: 'Werk jij in Gent?', example: 'van: jij werkt in Gent' },
              ],
            },
            {
              id: '1.4.2',
              title: 'Getallen 20–100',
              type: LESSON_TYPES.NUMBERS,
              intro:
                'Let op de volgorde: eerst het eenheidsgetal, dan "en", dan het tiental. "eenentwintig" = 1 + en + 20.',
              items: [
                { nl: 'twintig', value: 20 },
                { nl: 'eenentwintig', value: 21, tip: 'een + en + twintig' },
                { nl: 'dertig', value: 30 },
                { nl: 'veertig', value: 40 },
                { nl: 'vijftig', value: 50 },
                { nl: 'zestig', value: 60 },
                { nl: 'zeventig', value: 70 },
                { nl: 'tachtig', value: 80, tip: 'Let op: begint met "t".' },
                { nl: 'negentig', value: 90 },
                { nl: 'honderd', value: 100 },
              ],
            },
            {
              id: '1.4.3',
              title: 'Bij de winkel & de bakker',
              type: LESSON_TYPES.PHRASES,
              intro: 'Zinnen die je meteen kunt gebruiken bij de bakker of in de winkel in Gent.',
              items: [
                { nl: 'Hoeveel kost dit?', darija: 'بشحال هادا؟', darijaLat: 'bch7al hada?' },
                { nl: 'Ik wil graag een brood.', darija: 'بغيت خبزة عافاك', darijaLat: 'bghit khobza afak' },
                { nl: 'Mag ik een koffie?', darija: 'واش يمكن قهوة؟', darijaLat: 'wach ymken qahwa?' },
                { nl: 'Dat is te duur.', darija: 'هادا غالي بزاف', darijaLat: 'hada ghali bezzaf' },
                { nl: 'Heeft u wisselgeld?', darija: 'واش عندك الصرف؟', darijaLat: 'wach 3andek serf?' },
                { nl: 'Alleen dit, dank u.', darija: 'غير هادا، شكرا', darijaLat: 'ghir hada, choukran' },
              ],
            },
          ],
        },
      ],
    },
  ],
}

// -- Handige helpers voor de UI ------------------------------------------------

/** Aantal lessen in een level. */
export function countLessons(level) {
  return level.modules.reduce((sum, m) => sum + m.lessons.length, 0)
}

/** Platte lijst met alle lessen-id's (voor voortgangsberekening). */
export function allLessonIds(level) {
  return level.modules.flatMap((m) => m.lessons.map((l) => l.id))
}

/** Zoek een level op id. */
export function getLevel(id) {
  return curriculum.levels.find((l) => l.id === id)
}

export default curriculum
