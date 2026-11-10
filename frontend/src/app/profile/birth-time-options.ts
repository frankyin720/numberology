/** 十二時辰（以北京時間區間表示，子時跨日） */
export const BIRTH_TIME_SLOTS = [
  { value: 'ZI', label: '子時', range: '23:00–01:00' },
  { value: 'CHOU', label: '丑時', range: '01:00–03:00' },
  { value: 'YIN', label: '寅時', range: '03:00–05:00' },
  { value: 'MAO', label: '卯時', range: '05:00–07:00' },
  { value: 'CHEN', label: '辰時', range: '07:00–09:00' },
  { value: 'SI', label: '巳時', range: '09:00–11:00' },
  { value: 'WU', label: '午時', range: '11:00–13:00' },
  { value: 'WEI', label: '未時', range: '13:00–15:00' },
  { value: 'SHEN', label: '申時', range: '15:00–17:00' },
  { value: 'YOU', label: '酉時', range: '17:00–19:00' },
  { value: 'XU', label: '戌時', range: '19:00–21:00' },
  { value: 'HAI', label: '亥時', range: '21:00–23:00' },
  { value: 'UNKNOWN', label: '不詳', range: '' },
] as const;

export type BirthTimeCode = (typeof BIRTH_TIME_SLOTS)[number]['value'];

export const DEFAULT_BIRTH_TIME: BirthTimeCode = 'UNKNOWN';

export function birthTimeSelectLabel(slot: (typeof BIRTH_TIME_SLOTS)[number]): string {
  return slot.range ? `${slot.label}（${slot.range}）` : slot.label;
}

const SLOT_BY_VALUE = new Map(BIRTH_TIME_SLOTS.map((s) => [s.value, s]));

export function formatBirthTimeStored(code: string): string {
  const slot = SLOT_BY_VALUE.get(code as BirthTimeCode);
  if (!slot) {
    return code;
  }
  return birthTimeSelectLabel(slot);
}

/** 將非標準字串（如「6:00」）盡量對應到時辰代碼 */
export function resolveBirthTimeValue(stored: string | undefined | null): BirthTimeCode {
  const raw = (stored ?? '').trim();
  if (!raw) {
    return DEFAULT_BIRTH_TIME;
  }
  if (SLOT_BY_VALUE.has(raw as BirthTimeCode)) {
    return raw as BirthTimeCode;
  }
  if (/不詳|未知|不知道/.test(raw)) {
    return 'UNKNOWN';
  }
  for (const slot of BIRTH_TIME_SLOTS) {
    if (raw.includes(slot.label)) {
      return slot.value;
    }
  }
  const hm = raw.match(/(\d{1,2})\s*[:：]\s*(\d{1,2})/);
  if (hm) {
    const hour = Number(hm[1]);
    if (hour >= 0 && hour <= 23) {
      return hourToSlot(hour);
    }
  }
  return DEFAULT_BIRTH_TIME;
}

function hourToSlot(hour: number): BirthTimeCode {
  if (hour === 23 || hour === 0) {
    return 'ZI';
  }
  if (hour >= 1 && hour <= 2) {
    return 'CHOU';
  }
  if (hour >= 3 && hour <= 4) {
    return 'YIN';
  }
  if (hour >= 5 && hour <= 6) {
    return 'MAO';
  }
  if (hour >= 7 && hour <= 8) {
    return 'CHEN';
  }
  if (hour >= 9 && hour <= 10) {
    return 'SI';
  }
  if (hour >= 11 && hour <= 12) {
    return 'WU';
  }
  if (hour >= 13 && hour <= 14) {
    return 'WEI';
  }
  if (hour >= 15 && hour <= 16) {
    return 'SHEN';
  }
  if (hour >= 17 && hour <= 18) {
    return 'YOU';
  }
  if (hour >= 19 && hour <= 20) {
    return 'XU';
  }
  if (hour >= 21 && hour <= 22) {
    return 'HAI';
  }
  return 'UNKNOWN';
}
