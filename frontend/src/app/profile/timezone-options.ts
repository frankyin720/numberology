/** 精選時區（涵蓋華人聚居與常見海外地區） */
export const TIMEZONE_IDS = [
  'Asia/Shanghai',
  'Asia/Hong_Kong',
  'Asia/Taipei',
  'Asia/Macau',
  'Asia/Singapore',
  'Asia/Kuala_Lumpur',
  'Asia/Manila',
  'Asia/Jakarta',
  'Asia/Bangkok',
  'Asia/Ho_Chi_Minh',
  'Asia/Tokyo',
  'Asia/Seoul',
  'Asia/Kolkata',
  'Asia/Dubai',
  'Europe/London',
  'Europe/Paris',
  'Europe/Berlin',
  'Europe/Moscow',
  'America/New_York',
  'America/Chicago',
  'America/Denver',
  'America/Los_Angeles',
  'America/Toronto',
  'America/Vancouver',
  'America/Mexico_City',
  'America/Sao_Paulo',
  'Australia/Sydney',
  'Australia/Melbourne',
  'Pacific/Auckland',
  'Africa/Cairo',
  'UTC',
] as const;

export const DEFAULT_TIMEZONE = 'Asia/Shanghai';

export interface TimezoneOption {
  value: string;
  label: string;
  offsetMinutes: number;
}

function getOffsetMinutes(timeZone: string): number {
  if (timeZone === 'UTC') {
    return 0;
  }
  try {
    const now = new Date();
    const parts = new Intl.DateTimeFormat('en-US', {
      timeZone,
      timeZoneName: 'shortOffset',
    }).formatToParts(now);
    const tzName = parts.find((p) => p.type === 'timeZoneName')?.value ?? 'GMT';
    const match = tzName.match(/GMT([+-])(\d{1,2})(?::?(\d{2}))?/i);
    if (!match) {
      return 0;
    }
    const sign = match[1] === '-' ? -1 : 1;
    const hours = Number(match[2]);
    const mins = match[3] ? Number(match[3]) : 0;
    return sign * (hours * 60 + mins);
  } catch {
    return 0;
  }
}

function formatUtcOffset(offsetMinutes: number): string {
  const sign = offsetMinutes >= 0 ? '+' : '-';
  const abs = Math.abs(offsetMinutes);
  const h = Math.floor(abs / 60);
  const m = abs % 60;
  return m ? `${sign}${h}:${String(m).padStart(2, '0')}` : `${sign}${h}`;
}

function toOption(value: string): TimezoneOption {
  const offsetMinutes = getOffsetMinutes(value);
  const offset = formatUtcOffset(offsetMinutes);
  const place = value === 'UTC' ? 'UTC' : value.replace(/_/g, ' ');
  return {
    value,
    label: `UTC${offset} · ${place}`,
    offsetMinutes,
  };
}

/** @param extra 已保存但不在清單內的 IANA 時區，會附加一項以便仍顯示 */
export function buildTimezoneOptions(extra?: string): TimezoneOption[] {
  const ids = new Set<string>([...TIMEZONE_IDS]);
  if (extra?.trim()) {
    ids.add(extra.trim());
  }
  return [...ids]
    .map(toOption)
    .sort((a, b) => a.offsetMinutes - b.offsetMinutes || a.value.localeCompare(b.value));
}
