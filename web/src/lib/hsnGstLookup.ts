interface HSNGSTEntry {
  chapter: string;
  hsnCodes: string[];
  gstRate: number;
}

const GSTRates = [0, 5, 12, 18, 28] as const;

const HSNGSTData: HSNGSTEntry[] = [
  // === Exempt / 0% Chapters & Headings ===
  // Chapter 01: Live animals (fresh)
  { chapter: '01', hsnCodes: ['0101','0102','0103','0104','0105'], gstRate: 0 },
  // Chapter 04: Dairy, eggs, honey
  { chapter: '04', hsnCodes: ['0401','0402','0403'], gstRate: 0 },
  // Chapter 07: Edible vegetables & potatoes (fresh)
  { chapter: '07', hsnCodes: ['0701','0702','0709'], gstRate: 0 },
  // Chapter 08: Edible fruit, nuts
  { chapter: '08', hsnCodes: ['0801','0802','0803','0804','0805','0806','0807','0808','0809','0810'], gstRate: 0 },
  // Chapter 09: Coffee, tea, spices
  { chapter: '09', hsnCodes: ['0901','0902'], gstRate: 0 },

  // === 5% GST Chapters & Headings ===
  // Chapter 06: Living trees, plants, bulbs, flowers
  { chapter: '06', hsnCodes: ['0601','0602'], gstRate: 5 },
  // Processed vegetables (Chapter 07)
  { chapter: '07', hsnCodes: ['0703','0704','0705','0706','0707','0708'], gstRate: 5 },
  // Processed fruit/nuts (Chapter 09)
  { chapter: '09', hsnCodes: ['0903','0904','0905','0906','0907','0908','0909'], gstRate: 5 },
  // Chapter 10: Cereals (rice, wheat)
  { chapter: '10', hsnCodes: ['1001','1002','1003','1004','1005'], gstRate: 5 },
  // Chapter 11: Products of milling industry (flour, meal)
  { chapter: '11', hsnCodes: ['1101','1102','1103','1104','1105','1106','1107','1108'], gstRate: 5 },
  // Chapter 15: Fats & oils of animal/vegetable origin
  { chapter: '15', hsnCodes: ['1501','1502','1503','1504','1505','1506','1507','1508','1509','1510','1511','1512','1513','1514','1515'], gstRate: 5 },
  // Prepared meat/fish (Chapter 16)
  { chapter: '16', hsnCodes: ['1601','1602','1603','1604'], gstRate: 5 },
  // Sugar & sugar confectionery (Chapter 17)
  { chapter: '17', hsnCodes: ['1701','1702','1703','1704'], gstRate: 5 },
  // Cacao, chocolate (Chapter 18)
  { chapter: '18', hsnCodes: ['1801','1802','1805','1806'], gstRate: 5 },
  // Cereals, flour preparations, pastries (Chapter 19)
  { chapter: '19', hsnCodes: ['1901','1902','1903','1904','1905'], gstRate: 5 },
  // Prepared vegetables/fruits/nuts (Chapter 20)
  { chapter: '20', hsnCodes: ['2001','2002','2003','2004','2005','2006','2007','2008','2009'], gstRate: 5 },
  // Coffee, tea, spices (prepared) (Chapter 21)
  { chapter: '21', hsnCodes: ['2101','2102','2103','2104','2106'], gstRate: 5 },
  // Chapter 24: Tobacco
  { chapter: '24', hsnCodes: ['2401','2402'], gstRate: 5 },
  // Gypsum plasters/cements (Chapter 25)
  { chapter: '25', hsnCodes: ['2501','2502','2503'], gstRate: 5 },
  // Iron/steel products (basic) (Chapter 27, 72, 73)
  { chapter: '27', hsnCodes: ['2710'], gstRate: 5 },

  // === 12% GST Chapters & Headings ===
  // Processed dairy/eggs (Chapter 04)
  { chapter: '04', hsnCodes: ['0404','0405','0406','0410'], gstRate: 12 },
  // Seeds/plants (processing) (Chapter 07, 08)
  { chapter: '07', hsnCodes: ['0713'], gstRate: 12 },
  { chapter: '08', hsnCodes: ['0811','0812','0813'], gstRate: 12 },
  // Spices (Chapter 09)
  { chapter: '09', hsnCodes: ['0904','0910'], gstRate: 12 },
  // Fats/oils processed (Chapter 15)
  { chapter: '15', hsnCodes: ['1516','1517','1518','1519','1520','1521','1522'], gstRate: 12 },
  // Cocoa/chocolate products (Chapter 18) & cereal preparations (Ch 19) - packaged
  { chapter: '18', hsnCodes: ['1803','1804','1805','1806'], gstRate: 12 },
  { chapter: '19', hsnCodes: ['1901','1902','1903','1904','1905','1908','1909'], gstRate: 12 },
  // Prepared foods (Ch 20)
  { chapter: '20', hsnCodes: ['2001','2002','2003','2004','2005','2006','2007','2008','2009'], gstRate: 12 },
  // Miscellaneous edible preparations (Ch 21)
  { chapter: '21', hsnCodes: ['2107','2108'], gstRate: 12 },
  // Sulphur, earths, stone/plaster (Ch 25)
  { chapter: '25', hsnCodes: ['2504','2505','2506','2507','2517','2518','2519','2523','2524','2525','2526','2527','2528','2530'], gstRate: 12 },
  // Salts, sulphur (Ch 28 - chemicals)
  { chapter: '28', hsnCodes: ['2801','2802','2803','2804','2805','2825','2826','2827','2829','2830','2831','2832','2833','2834','2835','2836','2839','2840','2841','2842','2843','2844'], gstRate: 12 },
  // Organic/Inorganic chemicals (Ch 29)
  { chapter: '29', hsnCodes: ['2901','2902','2903','2904','2905','2906','2907','2908','2909','2910','2911','2912','2913','2914','2915','2916','2917','2918','2919','2920'], gstRate: 12 },
  // Essential oils, cosmetic preparations (Ch 33 - some)
  { chapter: '33', hsnCodes: ['3301','3306','3307','3308'], gstRate: 12 },

  // === 18% GST Chapters & Headings ===
  // Chapter 30: Pharmaceuticals
  { chapter: '30', hsnCodes: ['3001','3002','3003','3004','3005','3006'], gstRate: 18 },
  // Photographic/cinematographic/office supplies (Ch 37)
  { chapter: '37', hsnCodes: ['3701','3702','3703','3704','3706','3707','3708'], gstRate: 18 },
  // Chapter 09 (some processed spices)
  { chapter: '09', hsnCodes: ['0910'], gstRate: 18 },

  // === 28% GST Chapters & Headings ===
  // Oils of volatile oils (Ch 12)
  { chapter: '12', hsnCodes: ['1201','1202','1203','1204'], gstRate: 28 },
  // Sugar confectionery/processed (luxury) (Ch 17, 19 - premium)
  { chapter: '17', hsnCodes: ['1704'], gstRate: 28 },
  { chapter: '18', hsnCodes: ['1806'], gstRate: 28 },
  // Beverages, spirits (Ch 22)
  { chapter: '22', hsnCodes: ['2201','2202','2203','2204','2205','2206','2207','2208'], gstRate: 28 },
  // Cosmetics/perfusory/toiletries (Ch 33) - many at 28%
  { chapter: '33', hsnCodes: ['3301','3302','3303','3304','3305'], gstRate: 28 },
  // Footwear, headgear, umbrellas (Ch 64, 65, 66)
  { chapter: '64', hsnCodes: ['6401','6402','6403','6404'], gstRate: 28 },
  { chapter: '65', hsnCodes: ['6501','6502','6503','6504','6505'], gstRate: 28 },
  { chapter: '66', hsnCodes: ['6601','6602','6603','6604'], gstRate: 28 },
  // Chapter 67: Articles of stone/plaster/glass/metal - luxury items
  { chapter: '67', hsnCodes: ['6701','6702','6703','6704'], gstRate: 28 },
  // Motor vehicles (Ch 87)
  { chapter: '87', hsnCodes: ['8701','8702','8703','8704','8705','8706','8707','8708'], gstRate: 28 },
  // Electrical machinery (Ch 85) - some luxury items
  { chapter: '85', hsnCodes: ['8501','8502','8503','8504','8505','8506','8507','8508','8509','8510'], gstRate: 28 },
];

// Build a flat lookup: code -> { rate: number, isExactMatch: boolean }
const HSNCodeMap = new Map<string, number>();
const HSNChapterMap = new Map<string, number[]>();

for (const entry of HSNGSTData) {
  for (const hsnCode of entry.hsnCodes) {
    const existingRate = HSNCodeMap.get(hsnCode);
    if (existingRate === undefined) {
      HSNCodeMap.set(hsnCode, entry.gstRate);
    } else if (Math.abs(existingRate - entry.gstRate) < 0.01) {
      // Consistent rate, keep existing
    }
    const chapterRates = HSNChapterMap.get(entry.chapter) || [];
    if (!chapterRates.includes(entry.gstRate)) {
      chapterRates.push(entry.gstRate);
    }
    HSNChapterMap.set(entry.chapter, chapterRates);
  }
}

function findLongestPrefixMatch(code: string): number | null {
  if (code.length === 0) return null;

  for (let len = Math.min(code.length, 4); len >= 2; len--) {
    const prefix = code.substring(0, len);
    if (HSNCodeMap.has(prefix)) {
      return HSNCodeMap.get(prefix) ?? null;
    }
  }

  const chapter = code.substring(0, 2);
  const chapterRates = HSNChapterMap.get(chapter);
  if (chapterRates && chapterRates.length > 0) {
    // If all rates for a chapter are the same, return it. Otherwise default to null.
    if (chapterRates.length === 1) {
      return chapterRates[0];
    }
    // Chapter has mixed rates — can't reliably guess
  }

  return null;
}

export interface HSNRateResult {
  rate: number | null;
  isExactMatch: boolean;
  matchedCode: string | null;
}

export function lookupHSNGSTRate(code: string): number | null {
  if (!code) return null;
  const trimmed = code.toUpperCase().trim();
  return findLongestPrefixMatch(trimmed);
}

export function lookupHSNGSTResult(code: string): HSNRateResult {
  if (!code) {
    return { rate: null, isExactMatch: false, matchedCode: null };
  }
  const trimmed = code.toUpperCase().trim();

  for (let len = Math.min(trimmed.length, 4); len >= 2; len--) {
    const prefix = trimmed.substring(0, len);
    if (HSNCodeMap.has(prefix)) {
      return {
        rate: HSNCodeMap.get(prefix) ?? null,
        isExactMatch: len === trimmed.length,
        matchedCode: prefix,
      };
    }
  }

  const chapter = trimmed.substring(0, 2);
  const chapterRates = HSNChapterMap.get(chapter);
  if (chapterRates && chapterRates.length === 1) {
    return {
      rate: chapterRates[0],
      isExactMatch: false,
      matchedCode: chapter,
    };
  }

  return { rate: null, isExactMatch: false, matchedCode: null };
}

export const VALID_GST_RATES = GSTRates;
