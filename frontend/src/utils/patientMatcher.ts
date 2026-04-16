import type { PatientInfo } from '@/types/patient'
import type { ParsedVoiceItem } from './voiceParser'

export interface MatchResult {
  status: 'matched' | 'not_found' | 'duplicate_name' | 'ambiguous'
  patientId?: number
  patient?: PatientInfo
  candidates?: PatientInfo[]
  message?: string
}

export function matchPatient(
  item: ParsedVoiceItem,
  patients: PatientInfo[],
): MatchResult {
  const { bedNumber, patientName } = item

  if (bedNumber && patientName) {
    return matchByBedAndName(bedNumber, patientName, patients)
  }

  if (bedNumber) {
    return matchByBedOnly(bedNumber, patients)
  }

  if (patientName) {
    return matchByNameOnly(patientName, patients)
  }

  return {
    status: 'not_found',
    message: '无法识别患者信息',
  }
}

function matchByBedAndName(
  bed: string,
  name: string,
  patients: PatientInfo[],
): MatchResult {
  const normalizedName = name.trim()
  const normalizedBed = normalizeBed(bed)

  const exactMatch = patients.find((p) => {
    return normalizeBed(p.bedNumber) === normalizedBed && p.name === normalizedName
  })

  if (exactMatch) {
    return {
      status: 'matched',
      patientId: exactMatch.id,
      patient: exactMatch,
    }
  }

  const bedMatches = patients.filter(
    (p) => normalizeBed(p.bedNumber) === normalizedBed,
  )

  if (bedMatches.length === 1) {
    return {
      status: 'ambiguous',
      patientId: bedMatches[0].id,
      patient: bedMatches[0],
      message: '床号匹配但姓名不符',
    }
  }

  if (bedMatches.length > 1) {
    return {
      status: 'duplicate_name',
      candidates: bedMatches,
      message: '床号匹配到多个患者',
    }
  }

  const nameMatches = patients.filter((p) => p.name === normalizedName)

  if (nameMatches.length === 1) {
    return {
      status: 'matched',
      patientId: nameMatches[0].id,
      patient: nameMatches[0],
    }
  }

  if (nameMatches.length > 1) {
    return {
      status: 'duplicate_name',
      candidates: nameMatches,
      message: '姓名匹配到多个患者',
    }
  }

  return {
    status: 'not_found',
    message: '未找到匹配的患者',
  }
}

function matchByBedOnly(bed: string, patients: PatientInfo[]): MatchResult {
  const normalizedBed = normalizeBed(bed)

  const matches = patients.filter(
    (p) => normalizeBed(p.bedNumber) === normalizedBed,
  )

  if (matches.length === 1) {
    return {
      status: 'matched',
      patientId: matches[0].id,
      patient: matches[0],
    }
  }

  if (matches.length > 1) {
    return {
      status: 'duplicate_name',
      candidates: matches,
      message: '床号匹配到多个患者',
    }
  }

  return {
    status: 'not_found',
    message: '未找到床号匹配的患者',
  }
}

function matchByNameOnly(name: string, patients: PatientInfo[]): MatchResult {
  const normalizedName = name.trim()

  const matches = patients.filter((p) => p.name === normalizedName)

  if (matches.length === 1) {
    return {
      status: 'matched',
      patientId: matches[0].id,
      patient: matches[0],
    }
  }

  if (matches.length > 1) {
    return {
      status: 'duplicate_name',
      candidates: matches,
      message: '姓名匹配到多个患者',
    }
  }

  return {
    status: 'not_found',
    message: '未找到姓名匹配的患者',
  }
}

function normalizeBed(bed: string): string {
  const num = bed.replace(/[床号第\s]/g, '')
  return num + '床'
}

export function batchMatchPatients(
  items: ParsedVoiceItem[],
  patients: PatientInfo[],
): Array<{ item: ParsedVoiceItem; match: MatchResult }> {
  return items.map((item) => ({
    item,
    match: matchPatient(item, patients),
  }))
}