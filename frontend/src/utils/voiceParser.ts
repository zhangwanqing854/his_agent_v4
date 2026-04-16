export interface ParsedVoiceItem {
  bedNumber: string
  patientName: string
  content: string
  isAmbiguous: boolean
}

export interface ParseResult {
  items: ParsedVoiceItem[]
  rawText: string
}

const BED_NAME_PATTERN = /(?:第)?(\d+)[床号]\s*(\S+?)\s*[，,、]\s*([^，,；。]+)/g
const BED_ONLY_PATTERN = /(?:第)?(\d+)[床号]\s*[，,、]\s*([^，,；。]+)/g
const NAME_ONLY_PATTERN = /(\S+?)\s*[，,、]\s*([^，,；。]+)/g

export function parseVoiceText(text: string): ParseResult {
  const items: ParsedVoiceItem[] = []
  const sentences = text.split(/[。；;]/g).filter((s) => s.trim())

  for (const sentence of sentences) {
    const trimmed = sentence.trim()
    const parsed = parseSentence(trimmed)
    if (parsed) {
      items.push(parsed)
    }
  }

  return {
    items,
    rawText: text,
  }
}

function parseSentence(sentence: string): ParsedVoiceItem | null {
  let bedNumber = ''
  let patientName = ''
  let content = ''
  let isAmbiguous = false

  const bedNameMatch = sentence.match(BED_NAME_PATTERN)
  if (bedNameMatch && bedNameMatch[1] && bedNameMatch[2] && bedNameMatch[3]) {
    bedNumber = bedNameMatch[1] + '床'
    patientName = bedNameMatch[2]
    content = bedNameMatch[3].trim()
  } else {
    const bedOnlyMatch = sentence.match(BED_ONLY_PATTERN)
    if (bedOnlyMatch && bedOnlyMatch[1] && bedOnlyMatch[2]) {
      bedNumber = bedOnlyMatch[1] + '床'
      content = bedOnlyMatch[2].trim()
      isAmbiguous = true
    } else {
      const nameOnlyMatch = sentence.match(NAME_ONLY_PATTERN)
      if (nameOnlyMatch && nameOnlyMatch[1] && nameOnlyMatch[2]) {
        patientName = nameOnlyMatch[1]
        content = nameOnlyMatch[2].trim()
        isAmbiguous = true
      } else {
        return null
      }
    }
  }

  return {
    bedNumber,
    patientName,
    content,
    isAmbiguous,
  }
}

export function normalizeBedNumber(bed: string): string {
  const num = bed.replace(/[床号第\s]/g, '')
  return num + '床'
}

export function extractBedNumber(text: string): string | null {
  const match = text.match(/(?:第)?(\d+)[床号]/)
  return match ? match[1] + '床' : null
}